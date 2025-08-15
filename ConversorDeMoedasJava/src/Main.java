package br.com.andre.luiz.conversor;

import com.google.gson.Gson; // Biblioteca para manipular JSON
import java.io.BufferedReader; // Para ler a resposta da API
import java.io.InputStreamReader; // Para ler os dados vindos da conexão
import java.net.HttpURLConnection; // Para conexão HTTP
import java.net.URL; // Para criar a URL da API
import java.util.HashMap; // Estrutura para armazenar moedas
import java.util.Locale; // Para configurar o scanner em pt-BR
import java.util.Map; // Para manipular mapas
import java.util.Scanner; // Para ler entrada do usuário

public class Main {

    // Chave da API Exchange Rate (precisa ser válida)
    private static final String CHAVE_API = "4737d719a14cbcf28a0eadd8";

    // Mapeamento de nomes comuns de moedas para siglas
    private static final Map<String, String> MOEDAS = new HashMap<>();
    static {
        MOEDAS.put("dólar", "USD");
        MOEDAS.put("dolar", "USD"); // sem acento
        MOEDAS.put("euro", "EUR");
        MOEDAS.put("real", "BRL");
        MOEDAS.put("libra", "GBP");
        MOEDAS.put("iene", "JPY");
        MOEDAS.put("franco suíço", "CHF");
        MOEDAS.put("yuan", "CNY");
    }

    /**
     * Busca a cotação da moeda de origem para a moeda destino usando a API
     * @param moedaOrigem Sigla da moeda de origem
     * @param moedaDestino Sigla da moeda destino
     * @return taxa de conversão ou 0 em caso de erro
     */
    public static double buscarCotacao(String moedaOrigem, String moedaDestino) {
        try {
            // Monta a URL da API
            String urlString = "https://v6.exchangerate-api.com/v6/" + CHAVE_API + "/latest/" + moedaOrigem;
            URL url = new URL(urlString);

            // Cria conexão HTTP
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("GET");

            // Lê a resposta da API linha por linha
            BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            StringBuilder conteudo = new StringBuilder();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                conteudo.append(linha);
            }
            leitor.close();
            conexao.disconnect();

            // Converte JSON para Map usando Gson
            Gson gson = new Gson();
            Map<String, Object> json = gson.fromJson(conteudo.toString(), Map.class);

            // Verifica se a API retornou sucesso
            if (!"success".equals(json.get("result"))) {
                System.out.println("Erro: falha ao obter dados da API.");
                return 0;
            }

            // Obtém mapa de taxas de conversão
            Map<String, Double> taxas = (Map<String, Double>) json.get("conversion_rates");

            // Retorna a taxa se a moeda destino existir
            if (taxas != null && taxas.containsKey(moedaDestino)) {
                System.out.println("Cotação atualizada em: " + json.get("time_last_update_utc"));
                return taxas.get(moedaDestino);
            } else {
                System.out.println("Erro: moeda " + moedaDestino + " não encontrada.");
                return 0;
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar cotação: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Converte nome de moeda para sigla ou retorna sigla se já estiver no formato correto
     */
    public static String nomeParaSigla(String entrada) {
        entrada = entrada.toLowerCase();
        if (MOEDAS.containsKey(entrada)) return MOEDAS.get(entrada);
        if (entrada.length() == 3) return entrada.toUpperCase(); // já é sigla
        return null; // inválida
    }

    /**
     * Solicita valor ao usuário e garante que seja um número
     */
    public static double scannerInputValor(Scanner scanner) {
        while (true) {
            System.out.print("Digite o valor a ser convertido: ");
            String valorStr = scanner.nextLine().replace(",", "."); // aceita vírgula
            try {
                return Double.parseDouble(valorStr);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido! Digite apenas números.");
            }
        }
    }

    /**
     * Mostra todas as moedas suportadas (nome + sigla)
     */
    public static void mostrarMoedasSuportadas() {
        System.out.println("Moedas suportadas (nome ou sigla):");
        for (Map.Entry<String, String> entry : MOEDAS.entrySet()) {
            System.out.printf("- %s (%s)%n", entry.getKey(), entry.getValue());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in).useLocale(new Locale("pt", "BR"));
        boolean continuar = true;

        while (continuar) {
            // Menu principal
            System.out.println("\n=== Conversor de Moedas ===");
            System.out.println("Escolha a opção de conversão:");
            System.out.println("1 - Real → Dólar");
            System.out.println("2 - Dólar → Real");
            System.out.println("3 - Real → Euro");
            System.out.println("4 - Euro → Real");
            System.out.println("5 - Real → Libra");
            System.out.println("6 - Libra → Real");
            System.out.println("7 - Conversão personalizada (qualquer moeda ou nome)");
            System.out.println("0 - Sair");
            System.out.print("Digite a opção: ");

            String entrada = scanner.nextLine();
            int opcao;
            try {
                opcao = Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida! Digite apenas números.");
                continue;
            }

            if (opcao == 0) {
                System.out.println("Saindo do programa...");
                break;
            }

            double valor, resultado;

            switch (opcao) {
                case 1: // Real → Dólar
                    valor = scannerInputValor(scanner);
                    resultado = valor / buscarCotacao("USD", "BRL");
                    System.out.printf("%.2f reais = %.2f dólares%n", valor, resultado);
                    break;
                case 2: // Dólar → Real
                    valor = scannerInputValor(scanner);
                    resultado = valor * buscarCotacao("USD", "BRL");
                    System.out.printf("%.2f dólares = %.2f reais%n", valor, resultado);
                    break;
                case 3: // Real → Euro
                    valor = scannerInputValor(scanner);
                    resultado = valor / buscarCotacao("EUR", "BRL");
                    System.out.printf("%.2f reais = %.2f euros%n", valor, resultado);
                    break;
                case 4: // Euro → Real
                    valor = scannerInputValor(scanner);
                    resultado = valor * buscarCotacao("EUR", "BRL");
                    System.out.printf("%.2f euros = %.2f reais%n", valor, resultado);
                    break;
                case 5: // Real → Libra
                    valor = scannerInputValor(scanner);
                    resultado = valor / buscarCotacao("GBP", "BRL");
                    System.out.printf("%.2f reais = %.2f libras%n", valor, resultado);
                    break;
                case 6: // Libra → Real
                    valor = scannerInputValor(scanner);
                    resultado = valor * buscarCotacao("GBP", "BRL");
                    System.out.printf("%.2f libras = %.2f reais%n", valor, resultado);
                    break;
                case 7: // Conversão personalizada
                    mostrarMoedasSuportadas();

                    System.out.print("Digite a moeda de origem (nome ou sigla): ");
                    String moedaOrigemInput = scanner.nextLine();
                    String moedaOrigem = nomeParaSigla(moedaOrigemInput);

                    System.out.print("Digite a moeda de destino (nome ou sigla): ");
                    String moedaDestinoInput = scanner.nextLine();
                    String moedaDestino = nomeParaSigla(moedaDestinoInput);

                    if (moedaOrigem == null || moedaDestino == null) {
                        System.out.println("Moeda inválida! Veja a lista de moedas suportadas.");
                        mostrarMoedasSuportadas();
                        break;
                    }

                    double taxa = buscarCotacao(moedaOrigem, moedaDestino);
                    if (taxa == 0) break;

                    valor = scannerInputValor(scanner);
                    resultado = valor * taxa;
                    System.out.printf("%.2f %s = %.2f %s%n", valor, moedaOrigem, resultado, moedaDestino);
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        }

        System.out.println("=== Conversor finalizado ===");
        scanner.close();
    }
}