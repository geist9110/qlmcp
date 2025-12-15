import { Duration } from "aws-cdk-lib";
import * as acm from "aws-cdk-lib/aws-certificatemanager";
import * as route53 from "aws-cdk-lib/aws-route53";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface DnsConstructProps extends BaseConstructProps {
  domainName: string;
  mainServerIp: string;
}

export class DnsConstruct extends BaseConstruct {
  public readonly hostedZone: route53.IHostedZone;
  public readonly certificate: acm.ICertificate;

  constructor(scope: Construct, id: string, props: DnsConstructProps) {
    super(scope, id, props);

    this.hostedZone = this.createHostedZone(props);
    this.createARecord(props);
    this.certificate = this.createCertificate(props);
  }

  private createHostedZone(props: DnsConstructProps): route53.IHostedZone {
    return route53.HostedZone.fromLookup(this, "hosted-zone", {
      domainName: props.domainName,
    });
  }

  private createARecord(props: DnsConstructProps): void {
    new route53.ARecord(this, "mcp-subdomain-record", {
      zone: this.hostedZone,
      recordName: "mcp",
      target: route53.RecordTarget.fromIpAddresses(props.mainServerIp),
      ttl: Duration.minutes(5),
      comment: "Route mcp subdomain to main server",
    });
  }

  private createCertificate(props: DnsConstructProps): acm.ICertificate {
    return new acm.Certificate(this, "certificate", {
      domainName: "mcp." + props.domainName,
      validation: acm.CertificateValidation.fromDns(this.hostedZone),
    });
  }
}
