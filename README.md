# oracle
Oracle is an application designed to calculate the Value at Risk (VaR) of a given portfolio. 
It leverages the MapReduce approach to handle large datasets and parallelize the computation process, and provide accurate risk assessments.

### Value at Risk
It represents the **maximum expected loss** over a certain time period.

### Conditional Value at Risk
This is defined as the expected loss **beyond** VaR, or more formally, we look at **the average of the distribution beyond the VaR**, that is of those returns which are less than the VaR.

## reference documentation
* [Understanding Value at Risk (VaR) and How It’s Computed](https://www.investopedia.com/terms/v/var.asp)
* [When Bloom filters don't bloom](https://blog.cloudflare.com/when-bloom-filters-dont-bloom)
* [Hibernate ON CONFLICT DO clause](https://vladmihalcea.com/hibernate-on-conflict-do-clause/)
