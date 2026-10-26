# Pharos Yield Optimizer Agent[cite: 10]

An intelligent agent developed in **Java** for the Pharos Agent Arena designed to compare yields across vaults, account for transaction costs and fees, and recommend or prepare rebalances securely[cite: 10].

## 📋 Requirements Met[cite: 10]

1. **Multi-Vault Comparison (Req. 01):** Compares at least 3 yield sources using live on-chain data[cite: 10].
2. **Cost and Period Accounting (Req. 02):** Accounts for gas, entry/exit fees, and a minimum holding period before recommending a move[cite: 10].
3. **Clear Recommendations (Req. 03):** Outputs a clear recommendation with expected net gain, or "stay put" with the reason[cite: 10].
4. **Signature Security (Req. 04):** Prepares transactions for user signature only; the agent never holds keys[cite: 10].

## 🚀 How to Run

1. Make sure you have **Java JDK** installed.
2. Compile the file:
   ```bash
   javac YieldOptimizer.java
