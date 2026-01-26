#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

# 1. Extract Docker DNS info BEFORE any flushing
DOCKER_DNS_RULES=$(iptables-save -t nat | grep "127\.0\.0\.11" || true)

# Flush existing rules and delete existing ipsets
iptables -F
iptables -X
iptables -t nat -F
iptables -t nat -X
iptables -t mangle -F
iptables -t mangle -X
ipset destroy allowed-domains 2>/dev/null || true

# 2. Selectively restore ONLY internal Docker DNS resolution
if [ -n "$DOCKER_DNS_RULES" ]; then
    echo "Restoring Docker DNS rules..."
    iptables -t nat -N DOCKER_OUTPUT 2>/dev/null || true
    iptables -t nat -N DOCKER_POSTROUTING 2>/dev/null || true
    echo "$DOCKER_DNS_RULES" | xargs -L 1 iptables -t nat
else
    echo "No Docker DNS rules to restore"
fi

# Allow DNS and localhost before any restrictions
iptables -A OUTPUT -p udp --dport 53 -j ACCEPT
iptables -A INPUT -p udp --sport 53 -j ACCEPT
iptables -A OUTPUT -p tcp --dport 22 -j ACCEPT
iptables -A INPUT -p tcp --sport 22 -m state --state ESTABLISHED -j ACCEPT
iptables -A INPUT -i lo -j ACCEPT
iptables -A OUTPUT -o lo -j ACCEPT

# Create ipset with CIDR support
ipset create allowed-domains hash:net

# ============================================================
# Add major CDN IP ranges (these cover most cloud services)
# ============================================================

echo "Adding Cloudflare IP ranges..."
# Cloudflare IPv4 ranges (api.anthropic.com uses Cloudflare)
for cidr in \
    "173.245.48.0/20" \
    "103.21.244.0/22" \
    "103.22.200.0/22" \
    "103.31.4.0/22" \
    "141.101.64.0/18" \
    "108.162.192.0/18" \
    "190.93.240.0/20" \
    "188.114.96.0/20" \
    "197.234.240.0/22" \
    "198.41.128.0/17" \
    "162.158.0.0/15" \
    "104.16.0.0/13" \
    "104.24.0.0/14" \
    "172.64.0.0/13" \
    "131.0.72.0/22"; do
    echo "  Adding Cloudflare $cidr"
    ipset add allowed-domains "$cidr" 2>/dev/null || true
done

echo "Adding Fastly IP ranges..."
# Fastly IPv4 ranges (Maven Central uses Fastly)
for cidr in \
    "23.235.32.0/20" \
    "43.249.72.0/22" \
    "103.244.50.0/24" \
    "103.245.222.0/23" \
    "103.245.224.0/24" \
    "104.156.80.0/20" \
    "140.248.64.0/18" \
    "140.248.128.0/17" \
    "146.75.0.0/17" \
    "151.101.0.0/16" \
    "157.52.64.0/18" \
    "167.82.0.0/17" \
    "167.82.128.0/20" \
    "167.82.160.0/20" \
    "167.82.224.0/20" \
    "172.111.64.0/18" \
    "185.31.16.0/22" \
    "199.27.72.0/21" \
    "199.232.0.0/16"; do
    echo "  Adding Fastly $cidr"
    ipset add allowed-domains "$cidr" 2>/dev/null || true
done

echo "Adding Akamai IP ranges (partial)..."
# Akamai major ranges
for cidr in \
    "23.0.0.0/12" \
    "104.64.0.0/10" \
    "184.24.0.0/13" \
    "184.50.0.0/15" \
    "184.84.0.0/14" \
    "2.16.0.0/13" \
    "95.100.0.0/15"; do
    echo "  Adding Akamai $cidr"
    ipset add allowed-domains "$cidr" 2>/dev/null || true
done

# ============================================================
# Fetch GitHub meta information
# ============================================================
echo "Fetching GitHub IP ranges..."
gh_ranges=$(curl -s https://api.github.com/meta)
if [ -n "$gh_ranges" ] && echo "$gh_ranges" | jq -e '.web and .api and .git' >/dev/null 2>&1; then
    echo "Processing GitHub IPs..."
    while read -r cidr; do
        if [[ "$cidr" =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}/[0-9]{1,2}$ ]]; then
            echo "  Adding GitHub $cidr"
            ipset add allowed-domains "$cidr" 2>/dev/null || true
        fi
    done < <(echo "$gh_ranges" | jq -r '(.web + .api + .git)[]' | aggregate -q 2>/dev/null || echo "$gh_ranges" | jq -r '(.web + .api + .git)[]')
else
    echo "WARNING: Could not fetch GitHub ranges, adding known ranges..."
    ipset add allowed-domains "140.82.112.0/20" 2>/dev/null || true
    ipset add allowed-domains "143.55.64.0/20" 2>/dev/null || true
    ipset add allowed-domains "185.199.108.0/22" 2>/dev/null || true
    ipset add allowed-domains "192.30.252.0/22" 2>/dev/null || true
    ipset add allowed-domains "20.201.28.0/24" 2>/dev/null || true
fi

# ============================================================
# Resolve specific domains as backup
# ============================================================
echo "Resolving additional domains..."
for domain in \
    "registry.npmjs.org" \
    "api.anthropic.com" \
    "sentry.io" \
    "statsig.anthropic.com" \
    "statsig.com" \
    "marketplace.visualstudio.com" \
    "vscode.blob.core.windows.net" \
    "update.code.visualstudio.com" \
    "repo.maven.apache.org" \
    "repo1.maven.org" \
    "central.sonatype.com" \
    "plugins.gradle.org" \
    "services.gradle.org" \
    "deb.debian.org" \
    "security.debian.org" \
    "archive.ubuntu.com" \
    "packages.microsoft.com"; do
    ips=$(dig +short +tries=2 +time=2 A "$domain" 2>/dev/null | grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$' || true)
    if [ -n "$ips" ]; then
        while read -r ip; do
            echo "  Adding $ip for $domain"
            ipset add allowed-domains "$ip" 2>/dev/null || true
        done <<< "$ips"
    fi
done

# ============================================================
# Host network access
# ============================================================
HOST_IP=$(ip route | grep default | cut -d" " -f3)
if [ -n "$HOST_IP" ]; then
    HOST_NETWORK=$(echo "$HOST_IP" | sed "s/\.[0-9]*$/.0\/24/")
    echo "Host network: $HOST_NETWORK"
    iptables -A INPUT -s "$HOST_NETWORK" -j ACCEPT
    iptables -A OUTPUT -d "$HOST_NETWORK" -j ACCEPT
fi

# Set default policies
iptables -P INPUT DROP
iptables -P FORWARD DROP
iptables -P OUTPUT DROP

# Allow established connections
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Allow traffic to whitelisted IPs
iptables -A OUTPUT -m set --match-set allowed-domains dst -j ACCEPT

# Reject everything else
iptables -A OUTPUT -j REJECT --reject-with icmp-admin-prohibited

echo ""
echo "=== Firewall configured ==="
echo "Testing connectivity..."

# Test blocked site
if curl --connect-timeout 3 -s https://example.com >/dev/null 2>&1; then
    echo "❌ ERROR: example.com reachable (should be blocked)"
else
    echo "✓ example.com blocked"
fi

# Test Anthropic
if curl --connect-timeout 5 -s https://api.anthropic.com >/dev/null 2>&1; then
    echo "✓ api.anthropic.com reachable"
else
    echo "❌ WARNING: api.anthropic.com not reachable"
fi

# Test Maven
if curl --connect-timeout 5 -s -I https://repo.maven.apache.org/maven2/ >/dev/null 2>&1; then
    echo "✓ repo.maven.apache.org reachable"
else
    echo "❌ WARNING: repo.maven.apache.org not reachable"
fi

echo "=== Done ==="