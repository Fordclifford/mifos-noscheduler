package org.apache.fineract.portfolio.loanaccount.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ReceiptNumberExistException
  extends AbstractPlatformDomainRuleException
{
  public ReceiptNumberExistException(String receiptNumber)
  {
    super("error.msg.receipt.number.already.used", "Receipt number " + receiptNumber + " already used", new Object[] { receiptNumber });
  }
}

/* Location:
 * Qualified Name:     org.apache.fineract.portfolio.loanaccount.exception.ReceiptNumberExistException
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */