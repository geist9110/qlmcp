import { Duration } from "aws-cdk-lib";
import * as acm from "aws-cdk-lib/aws-certificatemanager";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as targets from "aws-cdk-lib/aws-elasticloadbalancingv2-targets";
import { Construct } from "constructs";
import { BaseConstruct, BaseConstructProps } from "../core/baseConstruct";

interface LoadBalancerConstructProps extends BaseConstructProps {
  vpc: ec2.Vpc;
  mainServerInstance: ec2.Instance;
  certification: acm.ICertificate;
}

export class LoadBalancerConstruct extends BaseConstruct {
  public readonly loadBalancer: elbv2.ApplicationLoadBalancer;
  public readonly securityGroup: ec2.SecurityGroup;
  public readonly targetGroup: elbv2.ApplicationTargetGroup;

  constructor(scope: Construct, id: string, props: LoadBalancerConstructProps) {
    super(scope, id, props);

    this.securityGroup = this.createSecurityGroup(props);
    this.targetGroup = this.createTargetGroup(props, this.securityGroup);
    this.loadBalancer = this.createApplicationLoadBalancer(
      props,
      this.securityGroup,
    );

    this.addHttpListener(this.loadBalancer);
    this.addHttpsListener(
      this.loadBalancer,
      this.targetGroup,
      props.certification,
    );
  }

  private createSecurityGroup(
    props: LoadBalancerConstructProps,
  ): ec2.SecurityGroup {
    const securityGroup = new ec2.SecurityGroup(this, "security-group", {
      vpc: props.vpc,
      allowAllOutbound: true,
      securityGroupName: `${props.project}-${props.envName}-alb-security-group`,
    });

    securityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      "Allow HTTP traffic from anywhere",
    );
    securityGroup.addIngressRule(
      ec2.Peer.anyIpv6(),
      ec2.Port.tcp(80),
      "Allow HTTP traffic from anywhere (IPv6)",
    );
    securityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(443),
      "Allow HTTPS traffic from anywhere",
    );
    securityGroup.addIngressRule(
      ec2.Peer.anyIpv6(),
      ec2.Port.tcp(443),
      "Allow HTTPS traffic from anywhere (IPv6)",
    );

    return securityGroup;
  }

  private createTargetGroup(
    props: LoadBalancerConstructProps,
    securityGroup: ec2.SecurityGroup,
  ): elbv2.ApplicationTargetGroup {
    const mainServerPort = 8080;
    props.mainServerInstance.connections.allowFrom(
      securityGroup,
      ec2.Port.tcp(mainServerPort),
    );

    return new elbv2.ApplicationTargetGroup(this, "target-group", {
      vpc: props.vpc,
      port: mainServerPort,
      protocol: elbv2.ApplicationProtocol.HTTP,
      targetType: elbv2.TargetType.INSTANCE,
      targets: [new targets.InstanceTarget(props.mainServerInstance, 8080)],
      healthCheck: {
        protocol: elbv2.Protocol.HTTP,
        path: "/actuator/health",
        interval: Duration.seconds(30),
      },
    });
  }

  private createApplicationLoadBalancer(
    props: LoadBalancerConstructProps,
    securityGroup: ec2.SecurityGroup,
  ): elbv2.ApplicationLoadBalancer {
    const applicationLoadBalancer = new elbv2.ApplicationLoadBalancer(
      this,
      "application-load-balancer",
      {
        vpc: props.vpc,
        internetFacing: true,
        securityGroup: securityGroup,
        loadBalancerName: `${props.project}-${props.envName}-alb`,
      },
    );

    return applicationLoadBalancer;
  }

  private addHttpListener(loadBalancer: elbv2.ApplicationLoadBalancer): void {
    loadBalancer
      .addListener("http", {
        port: 80,
        protocol: elbv2.ApplicationProtocol.HTTP,
        open: true,
      })
      .addAction("redirect-to-https", {
        action: elbv2.ListenerAction.redirect({
          protocol: elbv2.ApplicationProtocol.HTTPS,
          port: "443",
          permanent: true,
        }),
      });
  }

  private addHttpsListener(
    loadBalancer: elbv2.ApplicationLoadBalancer,
    targetGroup: elbv2.ApplicationTargetGroup,
    certification: acm.ICertificate,
  ): void {
    loadBalancer.addListener("https", {
      port: 443,
      protocol: elbv2.ApplicationProtocol.HTTPS,
      open: true,
      defaultTargetGroups: [targetGroup],
      certificates: [certification],
    });
  }
}
