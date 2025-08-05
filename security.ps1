# ✨ 登录 Exchange Online（如果未登录会提示）
Write-Host "🔑 Connecting to Exchange Online..."
Connect-ExchangeOnline

# ⭐ 配置参数
$groupMail = "test1@catacernis.onmicrosoft.com"  # 你的组邮箱
$appId = "2d0df75c-6bc1-446f-bcf0-a22aea96c9b3"                    # 你的 Azure AD 应用 client ID

# 获取 group 对象
$group = Get-DistributionGroup -Identity $groupMail
if (-not $group) {
    Write-Host "❌ 未找到安全组: $groupMail" -ForegroundColor Red
    Disconnect-ExchangeOnline
    exit
}

# 创建 Application Access Policy
Write-Host "🔐 Creating Application Access Policy..."
New-ApplicationAccessPolicy -AppId $appId `
    -PolicyScopeGroupId $group.ExternalDirectoryObjectId `
    -AccessRight RestrictAccess `
    -Description "Restrict access to GraphMeetingAccessGroup only"

# 查看已创建的 policy
Write-Host "✅ Checking Application Access Policies..."
Get-ApplicationAccessPolicy | Format-List

# 断开 Exchange Online
Disconnect-ExchangeOnline
Write-Host "🏁 Done!"
