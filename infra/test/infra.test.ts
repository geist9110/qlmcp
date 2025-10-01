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

  beforeEach(() => {
    app = new cdk.App();
    stack = new InfraStack(app, 'TestStack', domainName, env, {
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

    test("VPC가 하나의 Public Subnet을 가지는지 확인", () => {
      template.resourceCountIs('AWS::EC2::Subnet', 1);
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

      test("Ingress 규칙이 2개인지 테스트", () => {
        const securityGroups = template.findResources('AWS::EC2::SecurityGroup', {
          Properties: {
            GroupName: 'qlmcp-main-sg',
          }
        })

        const sgKey = Object.keys(securityGroups)[0];
        const ingressRules = securityGroups[sgKey].Properties.SecurityGroupIngress;
        expect(ingressRules).toHaveLength(2);
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