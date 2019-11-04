package org.apache.fineract.portfolio.loanaccount.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ReceiptNullException
  extends AbstractPlatformDomainRuleException
{
  public ReceiptNullException()
  {
    super("error.msg.receipt.number.required.", "Receipt number cannot be null", new Object[0]);
  }
}

/* Location:
 * Qualified Name:     org.apache.fineract.portfolio.loanaccount.exception.ReceiptNullException
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */