import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as route53 from "aws-cdk-lib/aws-route53";
import { LoadBalancerTarget } from "aws-cdk-lib/aws-route53-targets";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface DnsConstructProps extends BaseConstructProps {
  hostedZone: route53.IHostedZone;
  loadBalancer: elbv2.ApplicationLoadBalancer;
}

export class DnsConstruct extends BaseConstruct {
  constructor(scope: Construct, id: string, props: DnsConstructProps) {
    super(scope, id, props);

    this.createARecord(props);
  }

  private createARecord(props: DnsConstructProps): void {
    new route53.ARecord(this, "mcp-subdomain-record", {
      zone: props.hostedZone,
      recordName: "mcp",
      target: route53.RecordTarget.fromAlias(
        new LoadBalancerTarget(props.loadBalancer),
      ),
      comment: "Route mcp subdomain to Application LoadBalancer",
    });
  }
}
