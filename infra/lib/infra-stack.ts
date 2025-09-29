import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {Construct} from 'constructs';

export class InfraStack extends cdk.Stack {
  public readonly instance: ec2.Instance;
  public readonly vpc: ec2.Vpc;

  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    this.vpc = new ec2.Vpc(this, 'qlmcp-vpc', {
      maxAzs: 2,
      natGateways: 0,
      subnetConfiguration: [
        {
          name: 'Public',
          subnetType: ec2.SubnetType.PUBLIC,
          cidrMask: 24,
        },
      ],
    });

    const securityGroup = new ec2.SecurityGroup(this, 'InstanceSecurityGroup', {
      vpc: this.vpc,
      description: "Allow SSH and HTTP access",
      allowAllOutbound: true,
    });

    securityGroup.addIngressRule(
        ec2.Peer.anyIpv4(),
        ec2.Port.tcp(22),
        "Allow SSH access from anywhere"
    );

    securityGroup.addIngressRule(
        ec2.Peer.anyIpv4(),
        ec2.Port.tcp(80),
        "Allow HTTP access from anywhere"
    );

    this.instance = new ec2.Instance(this, "QLMCP Main server", {
      vpc: this.vpc,
      instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.T3,
          ec2.InstanceSize.MICRO
      ),
      machineImage: ec2.MachineImage.latestAmazonLinux2023(),
      securityGroup: securityGroup,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      userData: ec2.UserData.forLinux()
    });

    // User Data에 간단한 웹서버 설치 스크립트 추가
    this.instance.userData.addCommands(
        'yum update -y',
        'yum install -y httpd',
        'systemctl start httpd',
        'systemctl enable httpd',
        'echo "<h1>Hello from CDK!</h1>" > /var/www/html/index.html'
    );

    // 출력값 정의
    new cdk.CfnOutput(this, 'InstanceId', {
      value: this.instance.instanceId,
      description: 'EC2 Instance ID',
    });

    new cdk.CfnOutput(this, 'InstancePublicIp', {
      value: this.instance.instancePublicIp,
      description: 'EC2 Instance Public IP',
    });

    new cdk.CfnOutput(this, 'InstancePublicDnsName', {
      value: this.instance.instancePublicDnsName,
      description: 'EC2 Instance Public DNS Name',
    });
  }
}
