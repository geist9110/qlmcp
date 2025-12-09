import {
  RemovalPolicy,
  SecretValue,
  Stack,
  StackProps,
  Tags,
} from "aws-cdk-lib";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import { SecurityGroup } from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";
import * as ssm from "aws-cdk-lib/aws-ssm";
import { Construct, IConstruct } from "constructs";
import { MainServerConstruct } from "./compute/mainServerConstruct";
import { McpServerConstruct } from "./compute/mcpServerConstruct";
import { BaseConstructProps } from "./core/baseConstruct";
import { DnsConstruct } from "./dns/dnsConstruct";
import { NetworkConstruct } from "./network/networkConstruct";

export class InfraStack extends Stack {
  public readonly env: string;
  public readonly network: NetworkConstruct;
  public readonly mainServer: MainServerConstruct;
  public readonly mcpServer: McpServerConstruct;
  public readonly dns: DnsConstruct;
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

    this.mainServer = new MainServerConstruct(this, "main-server", {
      ...common,
      vpc: this.network.vpc,
    });

    this.mcpServer = new McpServerConstruct(this, "mcp-server", {
      ...common,
      vpc: this.network.vpc,
      mainServerSecurityGroup: this.mainServer.securityGroup,
    });

    this.dns = new DnsConstruct(this, "dns", {
      ...common,
      domainName: "qlmcp.com",
      mainServerIp: this.mainServer.instance.instancePublicIp,
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
      ec2.Peer.securityGroupId(this.mainServer.securityGroup.securityGroupId),
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
