import * as cdk from 'aws-cdk-lib';
import * as dotenv from 'dotenv';
import {Match, Template} from 'aws-cdk-lib/assertions';
import {InfraStack} from '../lib/infra-stack';

const env = "test";
dotenv.config({path: `env/.env.${env}`});

describe('InfraStack', () => {
  let app: cdk.App;
  let stack: InfraStack;
  let template: Template;
  const domainName = process.env.DOMAIN_NAME!;
  const databaseUser = process.env.DATABASE_USER!;
  const databasePassword = process.env.DATABASE_PASSWORD!;

  beforeEach(() => {
    app = new cdk.App();
    stack = new InfraStack(
        app,
        'TestStack',
        domainName,
        databaseUser,
        databasePassword,
        env, {
          env: {
            account: process.env.AWS_ACCOUNT_ID,
            region: process.env.AWS_REGION,
          },
        });
    template = Template.fromStack(stack);
  });

  describe('VPC 테스트', () => {
    test('하나의 VPC만 생성되는지 확인', () => {
      template.resourceCountIs('AWS::EC2::VPC', 1);
    })

    test("VPC가 /16 CIDR 블록을 가지는지 확인", () => {
      template.hasResourceProperties('AWS::EC2::VPC', {
        CidrBlock: Match.stringLikeRegexp('^10\\.0\\.0\\.0/16$')
      })
    })

    test("VPC에 NatGateway가 없는지 확인", () => {
      template.resourceCountIs('AWS::EC2::NatGateway', 0);
    })

    test("VPC가 2*2개의 Subnet을 가지는지 확인", () => {
      template.resourceCountIs('AWS::EC2::Subnet', 4);
    })

    test("Subnet의 CIDR 마스크가 /24인지 확인", () => {
      template.hasResourceProperties('AWS::EC2::Subnet', {
        MapPublicIpOnLaunch: true,
        CidrBlock: Match.stringLikeRegexp('^10\\.0\\.0\\.0/24$'),
      });
    })

    test("Internet Gateway가 생성되는지 확인", () => {
      template.resourceCountIs('AWS::EC2::InternetGateway', 1);
    })

    test("VPC가 Internet Gateway에 연결되는지 확인", () => {
      template.hasResourceProperties('AWS::EC2::Route',
          Match.objectLike({
            DestinationCidrBlock: '0.0.0.0/0',
            GatewayId: Match.anyValue(),
          })
      );
    })
  })

  describe("메인 서버 테스트", () => {
    test("인스턴스 타입 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-main-server'}
        ]),
        InstanceType: 't3.small',
      })
    })

    test("Machine Image 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-main-server'}
        ]),
        ImageId: Match.objectLike({
          Ref: Match.stringLikeRegexp('SsmParameterValue.*amazonlinuxlatestal2023.*')
        })
      })
    })

    test("Public Subnet에 배치되는지 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-main-server'}
        ]),
        SubnetId: Match.objectLike({
          Ref: Match.stringLikeRegexp('qlmcpvpcPublicSubnet.*')
        })
      })
    })

    test("Security Group 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-main-server'}
        ]),
        SecurityGroupIds: Match.arrayWith([
          Match.objectLike({
            'Fn::GetAtt': Match.arrayWith([
              Match.stringLikeRegexp('qlmcpmainsg.*'),
              'GroupId'
            ])
          })
        ])
      })
    })

    test("Role이 인스턴스에 연결되는지 테스트", () => {
      const resources = template.toJSON().Resources;

      // 1. qlmcp-main-server 인스턴스 찾기
      const [instanceId, instance] = Object.entries(resources).find(([_, res]: any) =>
          res.Type === 'AWS::EC2::Instance' &&
          res.Properties.Tags?.some((t: any) => t.Key === 'Name' && t.Value === 'qlmcp-main-server')
      ) as [string, any];

      expect(instance).toBeDefined();

      // 2. 인스턴스가 참조하는 Profile 찾기
      const profileRef = instance.Properties.IamInstanceProfile.Ref;
      expect(profileRef).toBeDefined();

      // 3. Profile 찾기
      const profile = resources[profileRef];
      expect(profile.Type).toBe('AWS::IAM::InstanceProfile');

      // 4. Profile이 참조하는 Role 찾기
      const roleRef = profile.Properties.Roles[0].Ref;
      expect(roleRef).toMatch(/qlmcpmainserverrole.*/i);

      // 5. Role이 올바른 권한을 가지는지
      const role = resources[roleRef];
      expect(role.Type).toBe('AWS::IAM::Role');
      expect(role.Properties.ManagedPolicyArns).toBeDefined();

      const policies = JSON.stringify(role.Properties.ManagedPolicyArns);
      expect(policies).toMatch("AmazonSSMManagedInstanceCore");
      expect(policies).toMatch("AmazonS3ReadOnlyAccess");
    })
  })


  describe("MCP 서버 테스트", () => {
    test("인스턴스 타입 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-mcp-server'}
        ]),
        InstanceType: 't3.micro',
      })
    })

    test("Machine Image 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-mcp-server'}
        ]),
        ImageId: Match.objectLike({
          Ref: Match.stringLikeRegexp('SsmParameterValue.*amazonlinuxlatestal2023.*')
        })
      })
    })

    test("Public Subnet에 배치되는지 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-mcp-server'}
        ]),
        SubnetId: Match.objectLike({
          Ref: Match.stringLikeRegexp('qlmcpvpcPublicSubnet.*')
        })
      })
    })

    test("Security Group 테스트", () => {
      template.hasResourceProperties("AWS::EC2::Instance", {
        Tags: Match.arrayWith([
          {Key: 'Name', Value: 'qlmcp-mcp-server'}
        ]),
        SecurityGroupIds: Match.arrayWith([
          Match.objectLike({
            'Fn::GetAtt': Match.arrayWith([
              Match.stringLikeRegexp('qlmcpmcpsg.*'),
              'GroupId'
            ])
          })
        ])
      })
    })

    test("Role이 인스턴스에 연결되는지 테스트", () => {
      const resources = template.toJSON().Resources;

      // 1. qlmcp-main-server 인스턴스 찾기
      const [instanceId, instance] = Object.entries(resources).find(([_, res]: any) =>
          res.Type === 'AWS::EC2::Instance' &&
          res.Properties.Tags?.some((t: any) => t.Key === 'Name' && t.Value === 'qlmcp-mcp-server')
      ) as [string, any];

      expect(instance).toBeDefined();

      // 2. 인스턴스가 참조하는 Profile 찾기
      const profileRef = instance.Properties.IamInstanceProfile.Ref;
      expect(profileRef).toBeDefined();

      // 3. Profile 찾기
      const profile = resources[profileRef];
      expect(profile.Type).toBe('AWS::IAM::InstanceProfile');

      // 4. Profile이 참조하는 Role 찾기
      const roleRef = profile.Properties.Roles[0].Ref;
      expect(roleRef).toMatch(/qlmcpmcpserverrole.*/i);

      // 5. Role이 올바른 권한을 가지는지
      const role = resources[roleRef];
      expect(role.Type).toBe('AWS::IAM::Role');
      expect(role.Properties.ManagedPolicyArns).toBeDefined();

      const policies = JSON.stringify(role.Properties.ManagedPolicyArns);
      expect(policies).toMatch("AmazonSSMManagedInstanceCore");
      expect(policies).toMatch("AmazonS3ReadOnlyAccess");
    })
  })

  describe("Security Group 테스트", () => {

    describe("메인 서버 Security Group 테스트", () => {

      test("IPv4에서 443 포트를 허용하는지 테스트", () => {
        template.hasResourceProperties("AWS::EC2::SecurityGroup", {
          GroupName: 'qlmcp-main-sg',
          SecurityGroupIngress: Match.arrayWith([
            Match.objectLike({
              IpProtocol: 'tcp',
              FromPort: 443,
              ToPort: 443,
              CidrIp: '0.0.0.0/0',
            })
          ])
        })
      })

      test("IPv6에서 443 포트를 허용하는지 테스트", () => {
        template.hasResourceProperties("AWS::EC2::SecurityGroup", {
          GroupName: 'qlmcp-main-sg',
          SecurityGroupIngress: Match.arrayWith([
            Match.objectLike({
              IpProtocol: 'tcp',
              FromPort: 443,
              ToPort: 443,
              CidrIpv6: '::/0',
            })
          ])
        })
      })

      test("IPv4에서 80 포트를 허용하는지 테스트", () => {
        template.hasResourceProperties("AWS::EC2::SecurityGroup", {
          GroupName: 'qlmcp-main-sg',
          SecurityGroupIngress: Match.arrayWith([
            Match.objectLike({
              IpProtocol: 'tcp',
              FromPort: 80,
              ToPort: 80,
              CidrIp: '0.0.0.0/0',
            })
          ])
        })
      })

      test("Ingress 규칙이 3개인지 테스트", () => {
        const securityGroups = template.findResources('AWS::EC2::SecurityGroup', {
          Properties: {
            GroupName: 'qlmcp-main-sg',
          }
        })

        const sgKey = Object.keys(securityGroups)[0];
        const ingressRules = securityGroups[sgKey].Properties.SecurityGroupIngress;
        expect(ingressRules).toHaveLength(3);
      })

      test("모든 아웃바운드 트래픽을 허용하는지 테스트", () => {
        template.hasResourceProperties("AWS::EC2::SecurityGroup", {
          GroupName: 'qlmcp-main-sg',
          SecurityGroupEgress: Match.arrayWith([
            Match.objectLike({
              IpProtocol: '-1',
              CidrIp: '0.0.0.0/0',
            })
          ])
        })
      })
    })

    describe("MCP 서버 Security Group 테스트", () => {
      test("메인 서버 Security Group에서 모든 트래픽을 허용하는지 테스트", () => {
        template.hasResourceProperties("AWS::EC2::SecurityGroup", {
          GroupName: 'qlmcp-mcp-sg',
          SecurityGroupIngress: Match.arrayWith([
            Match.objectLike({
              IpProtocol: '-1',
              SourceSecurityGroupId: Match.objectLike({
                'Fn::GetAtt': Match.arrayWith([
                  Match.stringLikeRegexp('qlmcpmainsg.*'),
                  'GroupId'
                ])
              })
            })
          ])
        })
      })

      test("모든 아웃바운드 트래픽을 허용하는지 테스트", () => {
        template.hasResourceProperties("AWS::EC2::SecurityGroup", {
          GroupName: 'qlmcp-mcp-sg',
          SecurityGroupEgress: Match.arrayWith([
            Match.objectLike({
              IpProtocol: '-1',
              CidrIp: '0.0.0.0/0',
            })
          ])
        })
      })
    })
  })

  describe("Route53 테스트", () => {
    test("A 레코드가 메인 서버 인스턴스를 가리키는지 테스트", () => {
      template.hasResourceProperties("AWS::Route53::RecordSet", {
        Name: `mcp.${domainName}.`,
        Type: 'A',
        ResourceRecords: Match.arrayWith([
          Match.objectLike({
            'Fn::GetAtt': Match.arrayWith([
              Match.stringLikeRegexp('qlmcpmainserver.*'),
              'PublicIp'
            ])
          })
        ]),
        TTL: '300',
      })
    })
  })
});