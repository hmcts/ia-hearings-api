#HMC to hearings API
module "servicebus-subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=master"
  name                  = "hmc-ia-subs-to-cft-${var.env}"
  namespace_name        = "hmc-servicebus-${var.env}"
  topic_name            = "hmc-to-cft-${var.env}"
  resource_group_name   = "hmc-shared-${var.env}"
}

# Create a subscription rule for the HMC to IA hearings API
resource "azurerm_servicebus_subscription_rule" "topic_filter_rule_et" {
  name            = "hmc-servicebus-${var.env}-subscription-rule-BFA1"
  subscription_id = module.servicebus-subscription.id
  filter_type     = "CorrelationFilter"
  correlation_filter {
    properties = {
      hmctsServiceId = "BFA1"
    }
  }
}

# Fetch the connection string from the HMC key vault
data "azurerm_key_vault" "hmc-key-vault" {
  name                = "hmc-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
}

data "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  key_vault_id = "${data.azurerm_key_vault.hmc-key-vault.id}"
  name         = "hmc-servicebus-connection-string"
}

# Store the connection string in the IA key vault
resource "azurerm_key_vault_secret" "hmc_to_ia_hearings_api_servicebus-connection-string" {
  name         = "hmc-servicebus-connection-string"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-connection-string.value
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

# Fetch the shared access key from the HMC key vault
data "azurerm_key_vault_secret" "hmc-servicebus-shared-access-key" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-shared-access-key"
}

# Store the shared access key in the IA key vault
resource "azurerm_key_vault_secret" "ia-hmc-servicebus-shared-access-key-tf" {
  name         = "hmc-servicebus-shared-access-key-tf"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-shared-access-key.value
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${data.azurerm_key_vault.ia_key_vault.id}"
  })
}
