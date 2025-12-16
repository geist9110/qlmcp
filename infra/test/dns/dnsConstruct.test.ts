import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as route53 from "aws-cdk-lib/aws-route53";
import { DnsConstruct } from "../../lib/dns/dnsConstruct";

describe("DnsConstruct Test", () => {
  let app: cdk.App;
  let stack: cdk.Stack;
  let template: Template;
  let construct: DnsConstruct;
  const domainName: string = "test.com";

  beforeEach(() => {
    app = new cdk.App();
    stack = new cdk.Stack(app, "test-stack", {
      env: {
        account: "test-account",
        region: "us-east-1",
      },
    });

    const vpc = new ec2.Vpc(stack, "test-vpc", { maxAzs: 2 });
    const loadBalancer = new elbv2.ApplicationLoadBalancer(stack, "test-alb", {
      vpc,
      internetFacing: true,
    });

    const hostedZone = new route53.PublicHostedZone(stack, "test-hosted-zone", {
      zoneName: domainName,
    });

    construct = new DnsConstruct(stack, "test-dns-construct", {
      project: "qlmcp",
      envName: "test",
      hostedZone,
      loadBalancer,
    });

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] creates alias A record for mcp subdomain", () => {
    template.hasResourceProperties(
      route53.CfnRecordSet.CFN_RESOURCE_TYPE_NAME,
      {
        Name: `mcp.${domainName}.`,
        Type: "A",
        AliasTarget: {
          DNSName: Match.objectLike({
            "Fn::Join": Match.arrayWith([
              "",
              Match.arrayWith([
                Match.stringLikeRegexp("dualstack\\..*"),
                Match.objectLike({
                  "Fn::GetAtt": [Match.anyValue(), "DNSName"],
                }),
              ]),
            ]),
          }),
          HostedZoneId: Match.objectLike({
            "Fn::GetAtt": [Match.anyValue(), "CanonicalHostedZoneID"],
          }),
        },
      },
    );
  });
});
