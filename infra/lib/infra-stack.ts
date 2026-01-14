import { Stack, StackProps } from "aws-cdk-lib";
import { Construct } from "constructs";
import { LoadBalancerConstruct } from "./compute/loadBalancerConstruct";
import { MainServerConstruct } from "./compute/mainServerConstruct";
import { McpServerConstruct } from "./compute/mcpServerConstruct";
import { BaseConstructProps } from "./core/baseConstruct";
import { CiCdConstruct } from "./data/cicdConstruct";
import { DatabaseConstruct } from "./data/databaseConstruct";
import { DnsConstruct } from "./dns/dnsConstruct";
import { ZoneConstruct } from "./dns/zoneConstruct";
import { NetworkConstruct } from "./network/networkConstruct";

export class InfraStack extends Stack {
  public readonly network: NetworkConstruct;
  public readonly mainServer: MainServerConstruct;
  public readonly mcpServer: McpServerConstruct;
  public readonly dns: DnsConstruct;
  public readonly database: DatabaseConstruct;
  public readonly cicd: CiCdConstruct;
  public readonly loadBalancer: LoadBalancerConstruct;
  public readonly zone: ZoneConstruct;

  constructor(scope: Construct, id: string, env: string, props?: StackProps) {
    super(scope, id, props);
    const common: BaseConstructProps = { envName: env, project: "qlmcp" };
    const domainName: string = "qlmcp.com";

    this.network = new NetworkConstruct(this, `network`, common);

    this.cicd = new CiCdConstruct(this, "cicd", common);

    this.mainServer = new MainServerConstruct(this, "main-server", {
      ...common,
      vpc: this.network.vpc,
      buildArtifactBucket: this.cicd.buildArtifactStorage,
    });

    this.mcpServer = new McpServerConstruct(this, "mcp-server", {
      ...common,
      vpc: this.network.vpc,
      mainServerSecurityGroup: this.mainServer.securityGroup,
    });

    this.zone = new ZoneConstruct(this, "zone", {
      ...common,
      domainName: domainName,
    });

    this.loadBalancer = new LoadBalancerConstruct(this, "load-balancer", {
      ...common,
      vpc: this.network.vpc,
      mainServerInstance: this.mainServer.instance,
      hostedZone: this.zone.hostedZone,
      domainName: domainName,
    });

    this.dns = new DnsConstruct(this, "dns", {
      ...common,
      hostedZone: this.zone.hostedZone,
      loadBalancer: this.loadBalancer.loadBalancer,
    });

    this.database = new DatabaseConstruct(this, "database", {
      ...common,
      vpc: this.network.vpc,
      mainServerSecurityGroup: this.mainServer.securityGroup,
    });
  }
}
