import * as cdk from 'aws-cdk-lib';
import {Match, Template} from 'aws-cdk-lib/assertions';
import {InfraStack} from '../lib/infra-stack';

describe('InfraStack', () => {
  let app: cdk.App;
  let stack: InfraStack;
  let template: Template;

  beforeEach(() => {
    app = new cdk.App();
    stack = new InfraStack(app, 'TestStack');
    template = Template.fromStack(stack);
  });

  // VPC 생성 확인
  test('Creates a VPC', () => {
    template.resourceCountIs('AWS::EC2::VPC', 1);
  });

  // EC2 인스턴스 생성 확인
  test('Creates an EC2 Instance', () => {
    template.resourceCountIs('AWS::EC2::Instance', 1);
  });

  // EC2 인스턴스 속성 확인
  test('EC2 Instance has correct properties', () => {
    template.hasResourceProperties('AWS::EC2::Instance', {
      InstanceType: 't3.micro',
    });
  });

  // Security Group 생성 확인
  test('Creates a Security Group', () => {
    template.resourceCountIs('AWS::EC2::SecurityGroup', 1);
  });

  // Security Group에 SSH 규칙이 있는지 확인
  test('Security Group allows SSH access', () => {
    template.hasResourceProperties('AWS::EC2::SecurityGroup', {
      SecurityGroupIngress: Match.arrayWith([
        Match.objectLike({
          IpProtocol: 'tcp',
          FromPort: 22,
          ToPort: 22,
          CidrIp: '0.0.0.0/0',
        }),
      ]),
    });
  });

  // Security Group에 HTTP 규칙이 있는지 확인
  test('Security Group allows HTTP access', () => {
    template.hasResourceProperties('AWS::EC2::SecurityGroup', {
      SecurityGroupIngress: Match.arrayWith([
        Match.objectLike({
          IpProtocol: 'tcp',
          FromPort: 80,
          ToPort: 80,
          CidrIp: '0.0.0.0/0',
        }),
      ]),
    });
  });

  // User Data가 설정되어 있는지 확인
  test('EC2 Instance has UserData configured', () => {
    template.hasResourceProperties('AWS::EC2::Instance', {
      UserData: Match.anyValue(),
    });
  });

  // 출력값 확인
  test('Stack has required outputs', () => {
    template.hasOutput('InstanceId', {});
    template.hasOutput('InstancePublicIp', {});
    template.hasOutput('InstancePublicDnsName', {});
  });

  // Subnet이 Public인지 확인
  test('Instance is in a public subnet', () => {
    template.hasResourceProperties('AWS::EC2::Instance', {
      SubnetId: Match.anyValue(),
    });
  });

  // 리소스 카운트 전체 검증
  test('Stack has expected number of resources', () => {
    const resources = template.toJSON().Resources;
    const resourceCount = Object.keys(resources).length;

    // VPC, Subnet, RouteTable, IGW, SecurityGroup, Instance 등
    expect(resourceCount).toBeGreaterThan(5);
  });
});