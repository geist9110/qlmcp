import {Duration, Stack, StackProps, Tags} from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as route53 from 'aws-cdk-lib/aws-route53';
import {Construct, IConstruct} from 'constructs';

export class InfraStack extends Stack {
  public readonly env: string;
  public readonly vpc: ec2.Vpc;
  public readonly mainServer: ec2.Instance;
  public readonly mcpServer: ec2.Instance;

  constructor(
      scope: Construct,
      id: string,
      domainName: string,
      env: string,
      props?: StackProps
  ) {
    super(scope, id, props);
    this.env = env;

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

    const mainServerSg = new ec2.SecurityGroup(this, 'qlmcp-main-sg', {
      vpc: this.vpc,
      allowAllOutbound: true,
      securityGroupName: 'qlmcp-main-sg',
      description: 'Security group for qlmcp main server',
    });

    mainServerSg.addIngressRule(
        ec2.Peer.anyIpv4(),
        ec2.Port.tcp(443),
        'Allow HTTPS traffic from anywhere(IPv4)',
    )

    mainServerSg.addIngressRule(
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
      securityGroup: mainServerSg,
    })
    this.addTags(this.mainServer, 'qlmcp-main-server');

    const mcpServerSg = new ec2.SecurityGroup(this, 'qlmcp-mcp-sg', {
      vpc: this.vpc,
      allowAllOutbound: true,
      securityGroupName: 'qlmcp-mcp-sg',
      description: 'Security group for qlmcp mcp server',
    })

    mcpServerSg.addIngressRule(
        ec2.Peer.securityGroupId(mainServerSg.securityGroupId),
        ec2.Port.allTraffic(),
        'Allow all traffic from qlmcp main server',
    )

    this.mcpServer = new ec2.Instance(this, 'qlmcp-mcp-server', {
      vpc: this.vpc,
      instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.T3,
          ec2.InstanceSize.MICRO,
      ),
      machineImage: ec2.MachineImage.latestAmazonLinux2023(),
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      securityGroup: mcpServerSg,
    })
    this.addTags(this.mcpServer, 'qlmcp-mcp-server');

    const hostedZone = route53.HostedZone.fromLookup(this, "HostedZone", {
      domainName: domainName
    });
    new route53.ARecord(this, 'McpSubdomainRecord', {
      zone: hostedZone,
      recordName: 'mcp',
      target: route53.RecordTarget.fromIpAddresses(
          this.mainServer.instancePublicIp
      ),
      ttl: Duration.minutes(5),
      comment: "Route mcp subdomain to main server"
    })
  }

  private addTags(resource: IConstruct, name: string) {
    Tags.of(resource).add("Name", name);
    Tags.of(resource).add("Project", "qlmcp");
    Tags.of(resource).add("Environment", this.env);
  }
}
