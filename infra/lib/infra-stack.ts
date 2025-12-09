import { Stack, StackProps, Tags } from "aws-cdk-lib";
import { Construct, IConstruct } from "constructs";
import { MainServerConstruct } from "./compute/mainServerConstruct";
import { McpServerConstruct } from "./compute/mcpServerConstruct";
import { BaseConstructProps } from "./core/baseConstruct";
import { DatabaseConstruct } from "./data/databaseConstruct";
import { DnsConstruct } from "./dns/dnsConstruct";
import { NetworkConstruct } from "./network/networkConstruct";

export class InfraStack extends Stack {
  public readonly env: string;
  public readonly network: NetworkConstruct;
  public readonly mainServer: MainServerConstruct;
  public readonly mcpServer: McpServerConstruct;
  public readonly dns: DnsConstruct;
  public readonly database: DatabaseConstruct;

  constructor(scope: Construct, id: string, env: string, props?: StackProps) {
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

    this.database = new DatabaseConstruct(this, "database", {
      ...common,
      vpc: this.network.vpc,
      mainServerSecurityGroup: this.mainServer.securityGroup,
    });
  }

  private addTags(resource: IConstruct, name: string) {
    Tags.of(resource).add("Name", name);
    Tags.of(resource).add("Project", "qlmcp");
    Tags.of(resource).add("Environment", this.env);
  }
}
