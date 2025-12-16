import * as cdk from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import * as route53 from "aws-cdk-lib/aws-route53";
import { ZoneConstruct } from "../../lib/dns/zoneConstruct";

describe("ZoneConstruct", () => {
  const domainName = "example.com";
  const hostedZoneId = "Z123456TEST";

  let app: cdk.App;
  let stack: cdk.Stack;
  let construct: ZoneConstruct;
  let template: Template;

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

    stack = new cdk.Stack(app, "zone-test-stack", {
      env: { account: "test-account", region: "us-east-1" },
    });

    construct = new ZoneConstruct(stack, "zone-construct", {
      project: "qlmcp",
      envName: "test",
      domainName,
    });

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] imports existing hosted zone via lookup", () => {
    expect(stack.resolve(construct.hostedZone.hostedZoneId)).toBe(hostedZoneId);
    expect(stack.resolve(construct.hostedZone.zoneName)).toBe(domainName);
  });

  test("[SUCCESS] does not create new hosted zone resources", () => {
    template.resourceCountIs(route53.CfnHostedZone.CFN_RESOURCE_TYPE_NAME, 0);
  });
});
