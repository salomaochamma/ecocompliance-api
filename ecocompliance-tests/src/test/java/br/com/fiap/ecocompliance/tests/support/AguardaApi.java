package br.com.fiap.ecocompliance.tests.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class AguardaApi {
    private static final int INTERVALO_SEGUNDOS = 3;

    private AguardaApi() {
    }

    public static void aguardar(String baseUrl, int tempoMaximoSegundos) {
        HttpClient cliente = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        String credenciais = "{\"email\":\"" + Config.ADMIN_EMAIL + "\",\"senha\":\"" + Config.ADMIN_SENHA + "\"}";
        HttpRequest login = HttpRequest.newBuilder(URI.create(baseUrl + "/auth/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(credenciais))
                .build();

        System.out.printf("[setup] Aguardando a EcoCompliance API em %s (até %ds)...%n", baseUrl, tempoMaximoSegundos);
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(tempoMaximoSegundos);
        String ultimoRetorno = "sem resposta";

        while (System.nanoTime() < limite) {
            try {
                HttpResponse<String> resposta = cliente.send(login, HttpResponse.BodyHandlers.ofString());
                if (resposta.statusCode() == 200) {
                    System.out.println("[setup] API disponível. Iniciando a suíte BDD.");
                    return;
                }
                ultimoRetorno = "HTTP " + resposta.statusCode();
            } catch (IOException e) {
                ultimoRetorno = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Espera pela API interrompida.", e);
            }
            dormir();
        }

        throw new IllegalStateException("A API não ficou disponível em " + baseUrl + " após "
                + tempoMaximoSegundos + "s (último retorno: " + ultimoRetorno + "). "
                + "Confira se a API está rodando e se o banco de dados está acessível.");
    }

    private static void dormir() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(INTERVALO_SEGUNDOS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
