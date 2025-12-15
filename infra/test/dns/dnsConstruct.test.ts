import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as acm from "aws-cdk-lib/aws-certificatemanager";
import * as route53 from "aws-cdk-lib/aws-route53";
import { DnsConstruct } from "../../lib/dns/dnsConstruct";

describe("DnsConstruct Test", () => {
  let app: cdk.App;
  let stack: cdk.Stack;
  let template: Template;
  let construct: DnsConstruct;
  const mainServerIp: string = "111.111.111.111";

  const hostedZoneId: string = "test-hosted-zone-id";
  const domainName: string = "test.com";

  beforeEach(() => {
    app = new cdk.App();
    app.node.setContext(
      `hosted-zone:account=test-account:domainName=${domainName}:region=us-east-1`,
      {
        Id: hostedZoneId,
        Name: domainName,
        CallerReference: "dummy",
      },
    );
    stack = new cdk.Stack(app, "test-stack", {
      env: {
        account: "test-account",
        region: "us-east-1",
      },
    });

    construct = new DnsConstruct(stack, "test-dns-construct", {
      project: "qlmcp",
      envName: "test",
      domainName: domainName,
      mainServerIp: mainServerIp,
    });

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] record test", () => {
    template.hasResourceProperties(
      route53.CfnRecordSet.CFN_RESOURCE_TYPE_NAME,
      {
        HostedZoneId: hostedZoneId,
        Name: `mcp.${domainName}.`,
        Type: "A",
        ResourceRecords: Match.arrayWith([mainServerIp]),
        TTL: "300",
      },
    );
  });

  test("[SUCCESS] certificate issued via DNS validation in hosted zone", () => {
    template.hasResourceProperties(acm.CfnCertificate.CFN_RESOURCE_TYPE_NAME, {
      DomainName: "mcp." + domainName,
      ValidationMethod: "DNS",
      DomainValidationOptions: Match.arrayWith([
        Match.objectLike({
          DomainName: "mcp." + domainName,
          HostedZoneId: hostedZoneId,
        }),
      ]),
    });
  });
});
