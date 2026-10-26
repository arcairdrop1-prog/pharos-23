import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class YieldOptimizer {

    public static class YieldSource {
        String name;
        double apy;
        double entryFeeUSD;
        double exitFeeUSD;
        int minHoldingDays;

        public YieldSource(String name, double apy, double entryFee, double exitFee, int minHoldingDays) {
            this.name = name;
            this.apy = apy;
            this.entryFeeUSD = entryFee;
            this.exitFeeUSD = exitFee;
            this.minHoldingDays = minHoldingDays;
        }
    }

    public static class OptimizationResult {
        String recommendation;
        double expectedNetGainUSD;
        String reason;
        String unsignedTxPayload;

        public OptimizationResult(String rec, double gain, String reason, String payload) {
            this.recommendation = rec;
            this.expectedNetGainUSD = gain;
            this.reason = reason;
            this.unsignedTxPayload = payload;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== YIELD OPTIMIZER REPORT ===\n");
            sb.append("Recommendation: ").append(recommendation).append("\n");
            sb.append(String.format("Expected Net Gain: $%.2f USD\n", expectedNetGainUSD));
            sb.append("Reason: ").append(reason).append("\n");
            if (unsignedTxPayload != null) {
                sb.append("Prepared Unsigned Tx: ").append(unsignedTxPayload).append("\n");
            }
            return sb.toString();
        }
    }

    // Requisito 01: Compara pelo menos 3 fontes de rendimento usando dados simulados on-chain
    private final List<YieldSource> yieldSources = new ArrayList<>();

    public YieldOptimizer() {
        yieldSources.add(new YieldSource("Pharos Vault A", 12.5, 5.0, 5.0, 30));
        yieldSources.add(new YieldSource("Pharos Vault B", 15.0, 15.0, 15.0, 14));
        yieldSources.add(new YieldSource("Pharos Vault C", 8.0, 2.0, 2.0, 7));
    }

    // Requisitos 02, 03 & 04: Contabiliza gás, taxas e período mínimo; emite recomendação ou "stay put"; prepara transação sem custódia de chaves
    public OptimizationResult evaluateRebalance(String currentVaultName, double capitalUSD, int plannedDays, double estimatedGasUSD) {
        YieldSource current = yieldSources.stream().filter(v -> v.name.equals(currentVaultName)).findFirst().orElse(null);
        if (current == null) {
            return new OptimizationResult("STAY PUT", 0.0, "Vault atual não encontrado.", null);
        }

        YieldSource bestTarget = null;
        double maxNetGain = -Double.MAX_VALUE;

        for (YieldSource target : yieldSources) {
            if (target.name.equals(currentVaultName)) continue;

            // Requisito 02: Verifica o período mínimo de retenção
            if (plannedDays < target.minHoldingDays) {
                continue; 
            }

            // Cálculo dos rendimentos brutos
            double currentAnnualReturn = capitalUSD * (current.apy / 100.0);
            double targetAnnualReturn = capitalUSD * (target.apy / 100.0);

            double currentPeriodReturn = currentAnnualReturn * (plannedDays / 365.0);
            double targetPeriodReturn = targetAnnualReturn * (plannedDays / 365.0);

            // Custos: saída do atual + entrada no alvo + taxas de gás
            double totalFees = current.exitFeeUSD + target.entryFeeUSD + estimatedGasUSD;

            double netGain = targetPeriodReturn - currentPeriodReturn - totalFees;

            if (netGain > maxNetGain) {
                maxNetGain = netGain;
                bestTarget = target;
            }
        }

        // Requisito 03: Se nenhum alvo compensar ou o ganho líquido for nulo/negativo, recomenda manter a posição ("stay put")
        if (bestTarget == null || maxNetGain <= 0) {
            return new OptimizationResult(
                "STAY PUT",
                0.0,
                "Permanecer em " + currentVaultName + " é mais rentável ou os custos de movimentação/períodos de retenção superam os ganhos.",
                null
            );
        }

        // Requisito 04: Prepara transações apenas para assinatura do utilizador. O agente nunca armazena chaves privadas.
        String unsignedTxPayload = String.format("ETH_SIGN_TYPED_DATA: Withdraw from %s -> Deposit to %s (Amount: $%.2f USD)",
                current.name, bestTarget.name, capitalUSD);

        return new OptimizationResult(
            "MOVE TO " + bestTarget.name,
            maxNetGain,
            String.format("Mover para %s gera um ganho líquido de $%.2f após contabilizar taxas de saída ($%.1f), entrada ($%.1f) e gás ($%.1f).",
                    bestTarget.name, maxNetGain, current.exitFeeUSD, bestTarget.entryFeeUSD, estimatedGasUSD),
            unsignedTxPayload
        );
    }

    public static void main(String[] args) {
        YieldOptimizer optimizer = new YieldOptimizer();

        System.out.println("=== YIELD OPTIMIZER SIMULATION ===");

        // Teste 1: Movimento lucrativo
        System.out.println("\nTeste 1: Capital $10,000 por 60 dias no Pharos Vault A");
        OptimizationResult res1 = optimizer.evaluateRebalance("Pharos Vault A", 10000.0, 60, 12.0);
        System.out.println(res1);

        // Teste 2: Permanecer na posição ("stay put") devido a período curto ou taxas elevadas
        System.out.println("\nTest 2: Capital $2,000 por 5 dias no Pharos Vault A");
        OptimizationResult res2 = optimizer.evaluateRebalance("Pharos Vault A", 2000.0, 5, 15.0);
        System.out.println(res2);
    }
}
