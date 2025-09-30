import {Stack, StackProps, Tags} from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {Construct, IConstruct} from 'constructs';

export class InfraStack extends Stack {
  public readonly vpc: ec2.Vpc;
  public readonly mainServer: ec2.Instance;

  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    this.vpc = new ec2.Vpc(this, 'qlmcp-vpc', {
      maxAzs: 1,
      natGateways: 0,
      subnetConfiguration: [
        {
          name: 'Public',
          subnetType: ec2.SubnetType.PUBLIC,
          cidrMask: 24,
        },
      ],
    });

    const mainSeverSg = new ec2.SecurityGroup(this, 'qlmcp-main-sg', {
      vpc: this.vpc,
      allowAllOutbound: true,
      securityGroupName: 'qlmcp-main-sg',
      description: 'Security group for qlmcp main server',
    });

    mainSeverSg.addIngressRule(
        ec2.Peer.anyIpv4(),
        ec2.Port.tcp(443),
        'Allow HTTPS traffic from anywhere(IPv4)',
    )

    mainSeverSg.addIngressRule(
        ec2.Peer.anyIpv6(),
        ec2.Port.tcp(443),
        'Allow HTTPS traffic from anywhere(IPv6)',
    )

    this.mainServer = new ec2.Instance(this, 'qlmcp-main-server', {
      vpc: this.vpc,
      instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.T3,
          ec2.InstanceSize.SMALL,
      ),
      machineImage: ec2.MachineImage.latestAmazonLinux2023(),
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      securityGroup: mainSeverSg,
    })
    this.addTags(this.mainServer, 'qlmcp-main-server');
  }

  private addTags(resource: IConstruct, name: string) {
    Tags.of(resource).add("Name", name);
    Tags.of(resource).add("Project", "qlmcp");
    Tags.of(resource).add("Environment", "development");
  }
}
