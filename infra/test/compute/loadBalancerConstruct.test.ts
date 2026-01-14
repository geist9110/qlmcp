import * as cdk from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import * as acm from "aws-cdk-lib/aws-certificatemanager";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as s3 from "aws-cdk-lib/aws-s3";
import { LoadBalancerConstruct } from "../../lib/compute/loadBalancerConstruct";
import { MainServerConstruct } from "../../lib/compute/mainServerConstruct";

describe("load balancer test", () => {
  let app: cdk.App;
  let stack: cdk.Stack;
  let template: Template;
  let mainServer: MainServerConstruct;
  let construct: LoadBalancerConstruct;
  const domainName = "test.com";
  let hostedZone: route53.IPublicHostedZone;

  beforeEach(() => {
    app = new cdk.App();
    stack = new cdk.Stack(app, "test-stack");

    const vpc = new ec2.Vpc(stack, "test-vpc", {
      maxAzs: 2,
      natGateways: 0,
    });

    mainServer = new MainServerConstruct(stack, "main-server", {
      project: "qlmcp",
      envName: "test",
      vpc: vpc,
      buildArtifactBucket: new s3.Bucket(stack, "test-bucket"),
    });

    hostedZone = new route53.PublicHostedZone(stack, "test-hosted-zone", {
      zoneName: domainName,
    });

    construct = new LoadBalancerConstruct(stack, "load-balancer-test", {
      project: "qlmcp",
      envName: "test",
      vpc: vpc,
      mainServerInstance: mainServer.instance,
      domainName,
      hostedZone,
    });

    template = Template.fromStack(stack);
  });

  test("[SUCCESS] ALB is internet facing with security group attached", () => {
    const securityGroupLogicalId = stack.getLogicalId(
      construct.securityGroup.node.defaultChild as ec2.CfnSecurityGroup,
    );

    template.hasResourceProperties(
      elbv2.CfnLoadBalancer.CFN_RESOURCE_TYPE_NAME,
      {
        Scheme: "internet-facing",
        Type: "application",
        SecurityGroups: Match.arrayWith([
          {
            "Fn::GetAtt": Match.arrayWith([securityGroupLogicalId, "GroupId"]),
          },
        ]),
      },
    );
  });

  test("[SUCCESS] Listeners: 80 redirects to 443, 443 forwards to target group", () => {
    template.hasResourceProperties(elbv2.CfnListener.CFN_RESOURCE_TYPE_NAME, {
      Port: 80,
      Protocol: elbv2.Protocol.HTTP,
      DefaultActions: Match.arrayWith([
        Match.objectLike({
          Type: "redirect",
          RedirectConfig: Match.objectLike({
            Protocol: elbv2.Protocol.HTTPS,
            Port: "443",
            StatusCode: "HTTP_301",
          }),
        }),
      ]),
    });

    template.hasResourceProperties(elbv2.CfnListener.CFN_RESOURCE_TYPE_NAME, {
      Port: 443,
      Protocol: "HTTPS",
      Certificates: Match.arrayWith([
        Match.objectLike({
          CertificateArn: { Ref: Match.stringLikeRegexp("certificate") },
        }),
      ]),
      DefaultActions: Match.arrayWith([
        Match.objectLike({
          Type: "forward",
          TargetGroupArn: { Ref: Match.stringLikeRegexp(".*") },
        }),
      ]),
    });
  });

  test("[SUCCESS] Target group registers main server instance", () => {
    const mainInstanceLogicalId = stack.getLogicalId(
      mainServer.instance.node.defaultChild as ec2.CfnInstance,
    );

    template.hasResourceProperties(
      elbv2.CfnTargetGroup.CFN_RESOURCE_TYPE_NAME,
      {
        Protocol: elbv2.Protocol.HTTP,
        Port: 8080,
        TargetType: elbv2.TargetType.INSTANCE,
        Targets: Match.arrayWith([
          Match.objectLike({
            Id: { Ref: mainInstanceLogicalId },
            Port: 8080,
          }),
        ]),
        HealthCheckProtocol: elbv2.Protocol.HTTP,
        HealthCheckPath: "/actuator/health",
        HealthCheckIntervalSeconds: 30,
      },
    );
  });

  test("[SUCCESS] Main server allows traffic from ALB security group", () => {
    const albSgLogicalId = stack.getLogicalId(
      construct.securityGroup.node.defaultChild as ec2.CfnSecurityGroup,
    );
    const mainSgLogicalId = stack.getLogicalId(
      mainServer.securityGroup.node.defaultChild as ec2.CfnSecurityGroup,
    );

    template.hasResourceProperties(
      ec2.CfnSecurityGroupIngress.CFN_RESOURCE_TYPE_NAME,
      {
        IpProtocol: ec2.Protocol.TCP,
        FromPort: 8080,
        ToPort: 8080,
        GroupId: { "Fn::GetAtt": [mainSgLogicalId, "GroupId"] },
        SourceSecurityGroupId: {
          "Fn::GetAtt": [albSgLogicalId, "GroupId"],
        },
      },
    );
  });

  test("[SUCCESS] Certificate issued via DNS validation", () => {
    const hostedZoneLogicalId = stack.getLogicalId(
      (hostedZone as route53.PublicHostedZone).node
        .defaultChild as route53.CfnHostedZone,
    );

    template.hasResourceProperties(acm.CfnCertificate.CFN_RESOURCE_TYPE_NAME, {
      DomainName: `mcp.${domainName}`,
      ValidationMethod: "DNS",
      DomainValidationOptions: Match.arrayWith([
        Match.objectLike({
          DomainName: `mcp.${domainName}`,
          HostedZoneId: { Ref: hostedZoneLogicalId },
        }),
      ]),
    });
  });
});
