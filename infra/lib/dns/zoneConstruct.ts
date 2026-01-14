import * as route53 from "aws-cdk-lib/aws-route53";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface ZoneConstructProps extends BaseConstructProps {
  domainName: string;
}

export class ZoneConstruct extends BaseConstruct {
  public readonly hostedZone: route53.IHostedZone;

  constructor(scope: Construct, id: string, props: ZoneConstructProps) {
    super(scope, id, props);

    this.hostedZone = this.createHostedZone(props);
  }

  private createHostedZone(props: ZoneConstructProps): route53.IHostedZone {
    return route53.HostedZone.fromLookup(this, "hosted-zone", {
      domainName: props.domainName,
    });
  }
}
