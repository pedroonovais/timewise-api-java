package com.timewise.timewise.service;

import com.timewise.timewise.model.Atividade;
import com.timewise.timewise.model.ScoreDiario;
import com.timewise.timewise.model.Usuario;
import com.timewise.timewise.repository.AtividadeRepository;
import com.timewise.timewise.repository.ScoreDiarioRepository;
import com.timewise.timewise.repository.UsuarioRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final ChatModel chatModel;
    private final AtividadeRepository atividadeRepository;
    private final ScoreDiarioRepository scoreDiarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ScoreDiarioService scoreDiarioService;

    public AIService(
            ChatModel chatModel,
            AtividadeRepository atividadeRepository,
            ScoreDiarioRepository scoreDiarioRepository,
            UsuarioRepository usuarioRepository,
            ScoreDiarioService scoreDiarioService) {
        
        this.chatModel = chatModel;
        this.atividadeRepository = atividadeRepository;
        this.scoreDiarioRepository = scoreDiarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.scoreDiarioService = scoreDiarioService;
    }

    public String gerarAnaliseProdutividade(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        LocalDate hoje = LocalDate.now();
        
        // Processamento Manual Síncrono: Garantir que os scores dos últimos 7 dias existam e estejam atualizados
        for (int i = 0; i < 7; i++) {
            LocalDate data = hoje.minusDays(i);
            try {
                scoreDiarioService.calcularESalvarScore(usuario.getId(), data);
            } catch (Exception e) {
                System.err.println("Aviso: Falha ao recalcular score síncrono para " + data + ": " + e.getMessage());
            }
        }

        LocalDate seteDiasAtrasData = hoje.minusDays(7);
        LocalDateTime seteDiasAtras = seteDiasAtrasData.atStartOfDay();
        LocalDateTime agora = LocalDateTime.now();
        
        List<Atividade> atividades = atividadeRepository.findByUsuarioAndTempoInicioBetween(usuario, seteDiasAtras, agora);
        
        List<ScoreDiario> scores = scoreDiarioRepository.findByUsuarioOrderByDataTrabalhoDesc(usuario).stream()
                .filter(s -> !s.getDataTrabalho().isBefore(seteDiasAtrasData))
                .collect(Collectors.toList());

        if (atividades.isEmpty() && scores.isEmpty()) {
            return "Olá " + usuario.getNome() + "! Ainda não tenho dados suficientes dos seus últimos 7 dias para gerar uma análise de produtividade. " +
                   "Continue registrando suas atividades e scores diários para que eu possa te ajudar!";
        }

        String prompt = construirPrompt(usuario, atividades, scores);

        try {
            return chatModel.call(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao comunicar com o assistente Spring AI: " + e.getMessage();
        }
    }

    private String construirPrompt(Usuario usuario, List<Atividade> atividades, List<ScoreDiario> scores) {
        String dadosAtividades = atividades.isEmpty() ? "Nenhuma atividade registrada neste período." : 
                atividades.stream()
                .limit(30) 
                .map(a -> String.format("- %s (%s): %s a %s", 
                        a.getNome(), a.getTipo(), a.getTempoInicio(), a.getTempoFim()))
                .collect(Collectors.joining("\n"));

        String dadosScores = scores.isEmpty() ? "Nenhum score registrado neste período." :
                scores.stream()
                .limit(7)
                .map(s -> String.format("- %s: Score %d", s.getDataTrabalho(), s.getValor()))
                .collect(Collectors.joining("\n"));

        return String.format("""
                Atue como um Coach de Produtividade e Bem-estar pessoal para o usuário %s.
                
                DADOS DOS ÚLTIMOS 7 DIAS:
                
                ATIVIDADES:
                %s
                
                SCORES DIÁRIOS (0-10):
                %s
                
                Com base ESTRITAMENTE nestes dados:
                1. Analise como o padrão de pausas (ou falta delas) está afetando os scores reportados.
                2. Se houver poucos dados, dê dicas gerais baseadas no pouco que vê.
                3. Dê 3 sugestões práticas direcionadas ao %s para a próxima semana.
                
                Trate o usuário pelo nome (%s). Seja empático e direto.
                """, usuario.getNome(), dadosAtividades, dadosScores, usuario.getNome(), usuario.getNome());
    }
}
