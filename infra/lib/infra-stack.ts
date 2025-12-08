import {
  Duration,
  RemovalPolicy,
  SecretValue,
  Stack,
  StackProps,
  Tags,
} from "aws-cdk-lib";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import { SecurityGroup } from "aws-cdk-lib/aws-ec2";
import * as iam from "aws-cdk-lib/aws-iam";
import * as rds from "aws-cdk-lib/aws-rds";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as ssm from "aws-cdk-lib/aws-ssm";
import { Construct, IConstruct } from "constructs";
import { BaseConstructProps } from "./core/baseConstruct";
import { NetworkConstruct } from "./network/networkConstruct";

export class InfraStack extends Stack {
  public readonly env: string;
  public readonly network: NetworkConstruct;
  public readonly mainServer: ec2.Instance;
  public readonly mcpServer: ec2.Instance;
  public readonly database: rds.DatabaseInstance;

  constructor(
    scope: Construct,
    id: string,
    domainName: string,
    databaseUser: string,
    databasePassword: string,
    env: string,
    props?: StackProps,
  ) {
    super(scope, id, props);
    this.env = env;
    const common: BaseConstructProps = { envName: env, project: "qlmcp" };

    this.network = new NetworkConstruct(this, `network`, common);

    const mainServerSg = new ec2.SecurityGroup(this, "qlmcp-main-sg", {
      vpc: this.network.vpc,
      allowAllOutbound: true,
      securityGroupName: "qlmcp-main-sg",
      description: "Security group for qlmcp main server",
    });

    mainServerSg.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(443),
      "Allow HTTPS traffic from anywhere(IPv4)",
    );

    mainServerSg.addIngressRule(
      ec2.Peer.anyIpv6(),
      ec2.Port.tcp(443),
      "Allow HTTPS traffic from anywhere(IPv6)",
    );

    mainServerSg.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      "Allow HTTP traffic from anywhere(IPv4)",
    );

    const mainServerRole = new iam.Role(this, "qlmcp-main-server-role", {
      assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com"),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName(
          "AmazonSSMManagedInstanceCore",
        ),
        iam.ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess"),
      ],
    });

    this.mainServer = new ec2.Instance(this, "qlmcp-main-server", {
      vpc: this.network.vpc,
      instanceType: ec2.InstanceType.of(
        ec2.InstanceClass.T3,
        ec2.InstanceSize.SMALL,
      ),
      machineImage: ec2.MachineImage.latestAmazonLinux2023(),
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      securityGroup: mainServerSg,
      role: mainServerRole,
    });
    this.addTags(this.mainServer, "qlmcp-main-server");

    const mcpServerSg = new ec2.SecurityGroup(this, "qlmcp-mcp-sg", {
      vpc: this.network.vpc,
      allowAllOutbound: true,
      securityGroupName: "qlmcp-mcp-sg",
      description: "Security group for qlmcp mcp server",
    });

    mcpServerSg.addIngressRule(
      ec2.Peer.securityGroupId(mainServerSg.securityGroupId),
      ec2.Port.allTraffic(),
      "Allow all traffic from qlmcp main server",
    );

    const mcpServerRole = new iam.Role(this, "qlmcp-mcp-server-role", {
      assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com"),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName(
          "AmazonSSMManagedInstanceCore",
        ),
        iam.ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess"),
      ],
    });

    this.mcpServer = new ec2.Instance(this, "qlmcp-mcp-server", {
      vpc: this.network.vpc,
      instanceType: ec2.InstanceType.of(
        ec2.InstanceClass.T3,
        ec2.InstanceSize.MICRO,
      ),
      machineImage: ec2.MachineImage.latestAmazonLinux2023(),
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      securityGroup: mcpServerSg,
      role: mcpServerRole,
    });
    this.addTags(this.mcpServer, "qlmcp-mcp-server");

    const hostedZone = route53.HostedZone.fromLookup(this, "HostedZone", {
      domainName: domainName,
    });
    new route53.ARecord(this, "McpSubdomainRecord", {
      zone: hostedZone,
      recordName: "mcp",
      target: route53.RecordTarget.fromIpAddresses(
        this.mainServer.instancePublicIp,
      ),
      ttl: Duration.minutes(5),
      comment: "Route mcp subdomain to main server",
    });

    // SSM Parameter store
    new ssm.StringParameter(this, "qlmcp-db-user", {
      parameterName: "/qlmcp/db/user",
      stringValue: databaseUser,
      tier: ssm.ParameterTier.STANDARD,
    });

    new ssm.StringParameter(this, "qlmcp-db-password", {
      parameterName: "/qlmcp/db/password",
      stringValue: databasePassword,
      tier: ssm.ParameterTier.STANDARD,
    });

    const databaseSG = new SecurityGroup(this, "qlmcp-databaseSG", {
      vpc: this.network.vpc,
      allowAllOutbound: true,
      securityGroupName: "qlmcp-database-sg",
      description: "Security group for qlmcp database",
    });

    databaseSG.addIngressRule(
      ec2.Peer.securityGroupId(mainServerSg.securityGroupId),
      ec2.Port.tcp(3306),
    );

    const database = new rds.DatabaseInstance(this, "qlmcp-database", {
      engine: rds.DatabaseInstanceEngine.mysql({
        version: rds.MysqlEngineVersion.VER_8_0,
      }),
      instanceType: ec2.InstanceType.of(
        ec2.InstanceClass.T3,
        ec2.InstanceSize.MICRO,
      ),
      vpc: this.network.vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
      },
      securityGroups: [databaseSG],
      multiAz: false,
      allocatedStorage: 20,
      maxAllocatedStorage: 100,
      databaseName: "qlmcp",
      credentials: rds.Credentials.fromPassword(
        databaseUser,
        SecretValue.unsafePlainText(databasePassword),
      ),
      removalPolicy: RemovalPolicy.DESTROY,
    });
  }

  private addTags(resource: IConstruct, name: string) {
    Tags.of(resource).add("Name", name);
    Tags.of(resource).add("Project", "qlmcp");
    Tags.of(resource).add("Environment", this.env);
  }
}
