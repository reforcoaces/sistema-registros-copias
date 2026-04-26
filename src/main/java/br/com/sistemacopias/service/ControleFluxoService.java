package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.EntradaControleForm;
import br.com.sistemacopias.dto.FluxoDashboardDto;
import br.com.sistemacopias.dto.MovimentoFluxoLinha;
import br.com.sistemacopias.dto.SaidaControleForm;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.model.CategoriaSaidaRecorrente;
import br.com.sistemacopias.model.EntradaControle;
import br.com.sistemacopias.model.MeioPagamentoEntrada;
import br.com.sistemacopias.model.OrderRecord;
import br.com.sistemacopias.model.RecorrenciaMensalidade;
import br.com.sistemacopias.model.SaidaControle;
import br.com.sistemacopias.model.SituacaoEntrada;
import br.com.sistemacopias.model.TipoEntradaControle;
import br.com.sistemacopias.repository.AlunoRepository;
import br.com.sistemacopias.repository.EntradaControleRepository;
import br.com.sistemacopias.repository.OrigemCompraRepository;
import br.com.sistemacopias.repository.OrderRepository;
import br.com.sistemacopias.repository.SaidaControleRepository;
import br.com.sistemacopias.support.OrigensCompraPadrao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ControleFluxoService {
    private final SaidaControleRepository saidaRepository;
    private final EntradaControleRepository entradaRepository;
    private final OrderRepository orderRepository;
    private final AlunoRepository alunoRepository;
    private final OrigemCompraRepository origemCompraRepository;

    public ControleFluxoService(
            SaidaControleRepository saidaRepository,
            EntradaControleRepository entradaRepository,
            OrderRepository orderRepository,
            AlunoRepository alunoRepository,
            OrigemCompraRepository origemCompraRepository) {
        this.saidaRepository = saidaRepository;
        this.entradaRepository = entradaRepository;
        this.orderRepository = orderRepository;
        this.alunoRepository = alunoRepository;
        this.origemCompraRepository = origemCompraRepository;
    }

    /** Sugestoes para o campo &quot;Onde foi comprado&quot;: padroes + origens guardadas pelo utilizador. */
    public List<String> listarOrigensCompraParaUi() {
        List<String> out = new ArrayList<>();
        for (String p : OrigensCompraPadrao.TODAS) {
            if (!listaContemIgnorandoMaiusculas(out, p)) {
                out.add(p);
            }
        }
        for (String c : origemCompraRepository.findAllCustom()) {
            if (!listaContemIgnorandoMaiusculas(out, c)) {
                out.add(c);
            }
        }
        return out;
    }

    public void registrarOrigemCompraUsada(String origemCompra) {
        String v = trimOrNull(origemCompra);
        if (v == null) {
            return;
        }
        if (OrigensCompraPadrao.contemIgnorandoMaiusculas(v)) {
            return;
        }
        origemCompraRepository.addIfAbsentIgnoreCase(v);
    }

    private static boolean listaContemIgnorandoMaiusculas(List<String> lista, String s) {
        if (s == null) {
            return false;
        }
        for (String x : lista) {
            if (x != null && x.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public FluxoDashboardDto montarDashboard(YearMonth mes, String filtroAlunoId) {
        gerarParcelasAutomaticasNoMes(mes);

        FluxoDashboardDto dto = new FluxoDashboardDto();
        dto.setMesReferencia(mes);

        String filtroId = trimOrNull(filtroAlunoId);
        if (filtroId != null) {
            dto.setFiltroAlunoId(filtroId);
            alunoRepository.findById(filtroId).ifPresent(a -> dto.setFiltroAlunoNome(a.getNomeCompleto()));
        }

        LocalDate ini = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();

        BigDecimal totalSaidas = saidaRepository.findAll().stream()
                .filter(s -> s.getDataCompraOuSaida() != null
                        && !s.getDataCompraOuSaida().isBefore(ini)
                        && !s.getDataCompraOuSaida().isAfter(fim))
                .map(SaidaControle::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalSaidasMes(totalSaidas);

        List<EntradaControle> entradasMes = entradaRepository.findAll().stream()
                .filter(e -> e.getDataHoraRegistro() != null && YearMonth.from(e.getDataHoraRegistro()).equals(mes))
                .toList();

        BigDecimal entManConf = entradasMes.stream()
                .filter(e -> e.getSituacao() == SituacaoEntrada.CONFIRMADA)
                .map(EntradaControle::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal entManPend = entradasMes.stream()
                .filter(e -> e.getSituacao() == SituacaoEntrada.PENDENTE)
                .map(EntradaControle::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalEntradasManuaisConfirmadasMes(entManConf);
        dto.setTotalEntradasManuaisPendentesMes(entManPend);
        dto.setTotalEntradasManuaisMes(entManConf.add(entManPend));

        if (filtroId != null) {
            BigDecimal filtroSum = entradasMes.stream()
                    .filter(e -> e.getSituacao() == SituacaoEntrada.CONFIRMADA)
                    .filter(e -> e.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO)
                    .filter(e -> filtroId.equals(e.getAlunoId()))
                    .map(EntradaControle::getValor)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalEntradasAlunoFiltradoConfirmadasMes(filtroSum);
        }

        BigDecimal entPed = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && YearMonth.from(o.getCreatedAt()).equals(mes))
                .map(OrderRecord::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalEntradasPedidosMes(entPed);

        Map<String, String> nomesAlunos = alunoRepository.findAll().stream()
                .collect(Collectors.toMap(Aluno::getId, Aluno::getNomeCompleto, (a, b) -> a));

        List<MovimentoFluxoLinha> linhas = new ArrayList<>();

        boolean filtrarMovimentos = filtroId != null;

        for (SaidaControle s : saidaRepository.findAll()) {
            if (s.getDataCompraOuSaida() == null
                    || s.getDataCompraOuSaida().isBefore(ini)
                    || s.getDataCompraOuSaida().isAfter(fim)) {
                continue;
            }
            if (filtrarMovimentos) {
                continue;
            }
            linhas.add(new MovimentoFluxoLinha(
                    s.getId(),
                    s.getDataHoraRegistro() != null ? s.getDataHoraRegistro() : s.getDataCompraOuSaida().atStartOfDay(),
                    true,
                    s.getDescricaoCompra(),
                    s.getValor(),
                    detalheSaida(s)));
        }

        for (EntradaControle e : entradaRepository.findAll()) {
            if (e.getDataHoraRegistro() == null || !YearMonth.from(e.getDataHoraRegistro()).equals(mes)) {
                continue;
            }
            if (filtrarMovimentos) {
                if (e.getTipo() != TipoEntradaControle.PAGAMENTO_ALUNO || !filtroId.equals(e.getAlunoId())) {
                    continue;
                }
            }
            String desc = descricaoEntrada(e, nomesAlunos);
            String meio = e.getMeioPagamento() != null ? e.getMeioPagamentoResumo() : "-";
            String sit = e.getSituacao() == SituacaoEntrada.PENDENTE ? " | Pendente" : "";
            boolean pendente = e.getSituacao() == SituacaoEntrada.PENDENTE;
            linhas.add(new MovimentoFluxoLinha(
                    e.getId(),
                    e.getDataHoraRegistro(),
                    false,
                    desc,
                    e.getValor(),
                    "Entrada manual | Meio: " + meio + sit + (e.getReferenciaCobranca() != null ? " | Ref: " + e.getReferenciaCobranca() : ""),
                    pendente));
        }

        for (OrderRecord o : orderRepository.findAll()) {
            if (o.getCreatedAt() == null || !YearMonth.from(o.getCreatedAt()).equals(mes)) {
                continue;
            }
            if (filtrarMovimentos) {
                continue;
            }
            String pg = o.getPaymentMethod() != null ? o.getPaymentMethod().getDescricao() : "";
            String desc = "Pedido copias / atendimento — " + pg;
            String det = "Origem: sistema Registro de copias | Pedido " + o.getId();
            linhas.add(new MovimentoFluxoLinha(
                    "order-" + o.getId(),
                    o.getCreatedAt(),
                    false,
                    desc,
                    o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO,
                    det));
        }

        linhas.sort(Comparator.comparing(MovimentoFluxoLinha::getDataHora).reversed());
        dto.setMovimentosDoMes(linhas);
        return dto;
    }

    private void gerarParcelasAutomaticasNoMes(YearMonth mes) {
        LocalDate ini = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        List<EntradaControle> todas = entradaRepository.findAll();
        for (Aluno a : alunoRepository.findAll()) {
            if (!temMensalidadeAtiva(a)) {
                continue;
            }
            if (temPendentePagamentoAluno(todas, a.getId())) {
                continue;
            }
            LocalDate prox = a.getProximaCobrancaPrevista();
            if (prox == null) {
                continue;
            }
            if (prox.isBefore(ini) || prox.isAfter(fim)) {
                continue;
            }
            String ref = "COB-" + a.getId() + "-" + prox;
            if (existeReferencia(todas, ref)) {
                continue;
            }
            String nota = "Mensalidade — " + (a.getRecorrenciaMensalidade() != null
                    ? a.getRecorrenciaMensalidade().getLabel()
                    : RecorrenciaMensalidade.MENSAL.getLabel());
            EntradaControle e = EntradaControle.mensalidadeAutomatica(
                    a.getId(),
                    a.getValorMensalidade(),
                    prox.atTime(12, 0),
                    ref,
                    nota);
            entradaRepository.save(e);
            todas.add(0, e);
        }
    }

    public void sincronizarMensalidadeAposSalvarAluno(String alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId).orElse(null);
        if (aluno == null) {
            return;
        }
        if (!temMensalidadeAtiva(aluno)) {
            removerPendentesAutomaticosAluno(alunoId);
            aluno.setProximaCobrancaPrevista(null);
            alunoRepository.save(aluno);
            return;
        }
        RecorrenciaMensalidade rec = aluno.getRecorrenciaMensalidade() != null
                ? aluno.getRecorrenciaMensalidade()
                : RecorrenciaMensalidade.MENSAL;
        aluno.setRecorrenciaMensalidade(rec);
        LocalDate reg = aluno.getCreatedAt() != null
                ? aluno.getCreatedAt().toLocalDate()
                : LocalDate.now();
        if (aluno.getProximaCobrancaPrevista() == null) {
            aluno.setProximaCobrancaPrevista(primeiraDataRecorrenteApos(reg, aluno.getDiaPagamentoPreferido(), rec));
        }
        alunoRepository.save(aluno);

        String refIns = refInscricao(alunoId);
        List<EntradaControle> todas = entradaRepository.findAll();
        Optional<EntradaControle> insOpt = todas.stream()
                .filter(e -> refIns.equals(e.getReferenciaCobranca()))
                .findFirst();
        if (insOpt.isEmpty()) {
            LocalDateTime dhIns = aluno.getCreatedAt() != null ? aluno.getCreatedAt() : LocalDateTime.now();
            EntradaControle ins = EntradaControle.mensalidadeAutomatica(
                    alunoId,
                    aluno.getValorMensalidade(),
                    dhIns,
                    refIns,
                    "Mensalidade na data de registo do aluno");
            entradaRepository.save(ins);
        } else {
            EntradaControle ins = insOpt.get();
            if (ins.getSituacao() == SituacaoEntrada.PENDENTE) {
                ins.setValor(aluno.getValorMensalidade());
                entradaRepository.save(ins);
            }
        }
    }

    public void confirmarEntrada(String entradaId) {
        EntradaControle e = entradaRepository.findById(entradaId)
                .orElseThrow(() -> new IllegalArgumentException("Entrada nao encontrada"));
        if (e.getSituacao() != SituacaoEntrada.PENDENTE) {
            throw new IllegalArgumentException("So e possivel confirmar entradas pendentes");
        }
        e.setSituacao(SituacaoEntrada.CONFIRMADA);
        if (e.getMeioPagamento() == MeioPagamentoEntrada.A_CONFIRMAR) {
            e.setMeioPagamento(MeioPagamentoEntrada.PIX);
        }
        entradaRepository.save(e);

        if (e.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO
                && e.getAlunoId() != null
                && e.isEntradaAutomaticaMensalidade()) {
            Aluno aluno = alunoRepository.findById(e.getAlunoId()).orElse(null);
            if (aluno != null && temMensalidadeAtiva(aluno)) {
                RecorrenciaMensalidade rec = aluno.getRecorrenciaMensalidade() != null
                        ? aluno.getRecorrenciaMensalidade()
                        : RecorrenciaMensalidade.MENSAL;
                LocalDate base = e.getDataHoraRegistro() != null
                        ? e.getDataHoraRegistro().toLocalDate()
                        : LocalDate.now();
                aluno.setProximaCobrancaPrevista(proximaDataAposPagamento(base, rec, aluno.getDiaPagamentoPreferido()));
                alunoRepository.save(aluno);
            }
        }
    }

    public BigDecimal somarEntradasConfirmadasPagamentoAluno(String alunoId, LocalDate dataInicio, LocalDate dataFim) {
        if (alunoId == null || dataInicio == null || dataFim == null) {
            return BigDecimal.ZERO;
        }
        LocalDateTime ini = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atStartOfDay();
        return entradaRepository.findAll().stream()
                .filter(e -> alunoId.equals(e.getAlunoId()))
                .filter(e -> e.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO)
                .filter(e -> e.getSituacao() == SituacaoEntrada.CONFIRMADA)
                .filter(e -> e.getDataHoraRegistro() != null
                        && !e.getDataHoraRegistro().isBefore(ini)
                        && e.getDataHoraRegistro().isBefore(fim))
                .map(EntradaControle::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<EntradaControle> listarEntradasFiltradas(
            String alunoId,
            SituacaoEntrada situacao,
            LocalDate dataInicio,
            LocalDate dataFim) {
        List<EntradaControle> all = entradaRepository.findAll();
        String aid = trimOrNull(alunoId);
        return all.stream()
                .filter(e -> aid == null || aid.equals(e.getAlunoId()))
                .filter(e -> situacao == null || e.getSituacao() == situacao)
                .filter(e -> {
                    if (dataInicio == null && dataFim == null) {
                        return true;
                    }
                    if (e.getDataHoraRegistro() == null) {
                        return false;
                    }
                    LocalDate d = e.getDataHoraRegistro().toLocalDate();
                    if (dataInicio != null && d.isBefore(dataInicio)) {
                        return false;
                    }
                    return dataFim == null || !d.isAfter(dataFim);
                })
                .sorted(Comparator.comparing(EntradaControle::getDataHoraRegistro).reversed())
                .toList();
    }

    private void removerPendentesAutomaticosAluno(String alunoId) {
        List<String> ids = entradaRepository.findAll().stream()
                .filter(e -> alunoId.equals(e.getAlunoId()))
                .filter(e -> e.getSituacao() == SituacaoEntrada.PENDENTE)
                .filter(EntradaControle::isEntradaAutomaticaMensalidade)
                .map(EntradaControle::getId)
                .toList();
        for (String id : ids) {
            entradaRepository.deleteById(id);
        }
    }

    private static boolean temMensalidadeAtiva(Aluno a) {
        return a.getValorMensalidade() != null && a.getValorMensalidade().compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean temPendentePagamentoAluno(List<EntradaControle> todas, String alunoId) {
        return todas.stream().anyMatch(e -> alunoId.equals(e.getAlunoId())
                && e.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO
                && e.getSituacao() == SituacaoEntrada.PENDENTE);
    }

    private static boolean existeReferencia(List<EntradaControle> todas, String ref) {
        return todas.stream().anyMatch(e -> ref.equals(e.getReferenciaCobranca()));
    }

    private static String refInscricao(String alunoId) {
        return "INS-" + alunoId;
    }

    /**
     * Primeira data de cobranca recorrente depois do cadastro (a inscricao em si e outra linha {@code INS-*}).
     */
    static LocalDate primeiraDataRecorrenteApos(LocalDate inicioRegistro, Integer diaPref, RecorrenciaMensalidade rec) {
        RecorrenciaMensalidade r = rec != null ? rec : RecorrenciaMensalidade.MENSAL;
        if (r.isPorSemanas()) {
            return inicioRegistro.plusWeeks(r.getSemanas());
        }
        int dia = diaPref != null ? Math.min(Math.max(diaPref, 1), 31) : 5;
        YearMonth ym = YearMonth.from(inicioRegistro);
        LocalDate cand = ym.atDay(Math.min(dia, ym.lengthOfMonth()));
        if (cand.isAfter(inicioRegistro)) {
            return cand;
        }
        YearMonth next = ym.plusMonths(r.getMeses());
        return next.atDay(Math.min(dia, next.lengthOfMonth()));
    }

    static LocalDate proximaDataAposPagamento(LocalDate pagamento, RecorrenciaMensalidade rec, Integer diaPref) {
        RecorrenciaMensalidade r = rec != null ? rec : RecorrenciaMensalidade.MENSAL;
        if (r.isPorSemanas()) {
            return pagamento.plusWeeks(r.getSemanas());
        }
        int dia = diaPref != null ? Math.min(Math.max(diaPref, 1), 31) : 5;
        YearMonth baseYm = YearMonth.from(pagamento);
        LocalDate cand = baseYm.plusMonths(r.getMeses()).atDay(Math.min(dia, baseYm.plusMonths(r.getMeses()).lengthOfMonth()));
        if (!cand.isAfter(pagamento)) {
            YearMonth n2 = baseYm.plusMonths(r.getMeses() * 2L);
            return n2.atDay(Math.min(dia, n2.lengthOfMonth()));
        }
        return cand;
    }

    private static String detalheSaida(SaidaControle s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Banco: ").append(s.getBancoPagamento() != null ? s.getBancoPagamento() : "-");
        sb.append(" | Data compra: ").append(s.getDataCompraOuSaida());
        if (s.getLinkCompra() != null && !s.getLinkCompra().isBlank()) {
            sb.append(" | Link: ").append(s.getLinkCompra().trim());
        }
        if (s.getOrigemCompra() != null && !s.getOrigemCompra().isBlank()) {
            sb.append(" | Onde: ").append(s.getOrigemCompra().trim());
        }
        return sb.toString();
    }

    private static String descricaoEntrada(EntradaControle e, Map<String, String> nomesAlunos) {
        if (e.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO) {
            String nome = e.getAlunoId() != null ? nomesAlunos.getOrDefault(e.getAlunoId(), "Aluno (id)") : "Aluno";
            String extra = e.getDescricaoLivre() != null && !e.getDescricaoLivre().isBlank()
                    ? " — " + e.getDescricaoLivre().trim()
                    : "";
            return "Mensalidade / pagamento — " + nome + extra;
        }
        return e.getDescricaoLivre() != null ? e.getDescricaoLivre().trim() : "Entrada";
    }

    public void criarSaida(SaidaControleForm form) {
        String desc = montarDescricaoSaida(form);
        SaidaControle s = SaidaControle.nova(
                desc,
                form.getValor(),
                form.getDataCompraOuSaida(),
                form.getBancoPagamento().trim());
        s.setCategoriaRecorrente(form.getCategoriaRecorrente());
        s.setLinkCompra(trimOrNull(form.getLinkCompra()));
        s.setOrigemCompra(trimOrNull(form.getOrigemCompra()));
        saidaRepository.save(s);
        registrarOrigemCompraUsada(s.getOrigemCompra());
    }

    public void atualizarSaida(SaidaControleForm form) {
        SaidaControle existente = saidaRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Saida nao encontrada"));
        existente.setDescricaoCompra(montarDescricaoSaida(form));
        existente.setValor(form.getValor());
        existente.setDataCompraOuSaida(form.getDataCompraOuSaida());
        existente.setBancoPagamento(form.getBancoPagamento().trim());
        existente.setCategoriaRecorrente(form.getCategoriaRecorrente());
        existente.setLinkCompra(trimOrNull(form.getLinkCompra()));
        existente.setOrigemCompra(trimOrNull(form.getOrigemCompra()));
        saidaRepository.save(existente);
        registrarOrigemCompraUsada(existente.getOrigemCompra());
    }

    private static String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String montarDescricaoSaida(SaidaControleForm form) {
        String extra = form.getDescricaoCompra() != null ? form.getDescricaoCompra().trim() : "";
        if (form.getCategoriaRecorrente() == CategoriaSaidaRecorrente.OUTRO) {
            if (extra.isEmpty()) {
                return CategoriaSaidaRecorrente.OUTRO.getLabel();
            }
            return extra;
        }
        if (extra.isEmpty()) {
            return form.getCategoriaRecorrente().getLabel();
        }
        return form.getCategoriaRecorrente().getLabel() + " — " + extra;
    }

    public SaidaControleForm paraFormSaida(SaidaControle s) {
        SaidaControleForm f = new SaidaControleForm();
        f.setId(s.getId());
        f.setValor(s.getValor());
        f.setDataCompraOuSaida(s.getDataCompraOuSaida());
        f.setBancoPagamento(s.getBancoPagamento());
        f.setLinkCompra(s.getLinkCompra());
        f.setOrigemCompra(s.getOrigemCompra());

        CategoriaSaidaRecorrente cat = s.getCategoriaRecorrente() != null
                ? s.getCategoriaRecorrente()
                : CategoriaSaidaRecorrente.OUTRO;
        f.setCategoriaRecorrente(cat);

        if (s.getCategoriaRecorrente() == null || cat == CategoriaSaidaRecorrente.OUTRO) {
            f.setDescricaoCompra(s.getDescricaoCompra());
        } else {
            String label = cat.getLabel();
            String d = s.getDescricaoCompra() != null ? s.getDescricaoCompra() : "";
            if (d.equals(label)) {
                f.setDescricaoCompra("");
            } else {
                String prefix = label + " — ";
                if (d.startsWith(prefix)) {
                    f.setDescricaoCompra(d.substring(prefix.length()).trim());
                } else {
                    f.setDescricaoCompra(d);
                }
            }
        }
        return f;
    }

    public void criarEntrada(EntradaControleForm form) {
        if (form.getMeioPagamento() == MeioPagamentoEntrada.A_CONFIRMAR) {
            throw new IllegalArgumentException("Meio reservado a entradas automaticas");
        }
        validarEntrada(form);
        String alunoId = null;
        if (form.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO
                && form.getAlunoId() != null
                && !form.getAlunoId().isBlank()) {
            alunoId = form.getAlunoId().trim();
        }
        String meioOutro = form.getMeioPagamento() == MeioPagamentoEntrada.OUTRO
                ? trimOrNull(form.getMeioPagamentoOutro())
                : null;
        EntradaControle e = EntradaControle.nova(
                form.getTipo(),
                alunoId,
                form.getDescricaoLivre() != null ? form.getDescricaoLivre().trim() : "",
                form.getValor(),
                form.getMeioPagamento(),
                meioOutro);
        entradaRepository.save(e);
    }

    public void atualizarEntrada(EntradaControleForm form) {
        EntradaControle existente = entradaRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Entrada nao encontrada"));
        if (existente.getSituacao() == SituacaoEntrada.PENDENTE && existente.isEntradaAutomaticaMensalidade()) {
            existente.setValor(form.getValor());
            existente.setDescricaoLivre(form.getDescricaoLivre() != null ? form.getDescricaoLivre().trim() : "");
            entradaRepository.save(existente);
            return;
        }
        if (form.getMeioPagamento() == MeioPagamentoEntrada.A_CONFIRMAR) {
            throw new IllegalArgumentException("Meio reservado a entradas automaticas");
        }
        validarEntrada(form);
        existente.setTipo(form.getTipo());
        if (form.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO
                && form.getAlunoId() != null
                && !form.getAlunoId().isBlank()) {
            existente.setAlunoId(form.getAlunoId().trim());
        } else {
            existente.setAlunoId(null);
        }
        existente.setDescricaoLivre(form.getDescricaoLivre() != null ? form.getDescricaoLivre().trim() : "");
        existente.setValor(form.getValor());
        existente.setMeioPagamento(form.getMeioPagamento());
        existente.setMeioPagamentoOutro(form.getMeioPagamento() == MeioPagamentoEntrada.OUTRO
                ? trimOrNull(form.getMeioPagamentoOutro())
                : null);
        entradaRepository.save(existente);
    }

    private void validarEntrada(EntradaControleForm form) {
        if (form.getTipo() == TipoEntradaControle.PAGAMENTO_ALUNO) {
            if (form.getAlunoId() == null || form.getAlunoId().isBlank()) {
                throw new IllegalArgumentException("Selecione o aluno");
            }
            alunoRepository.findById(form.getAlunoId())
                    .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        } else {
            if (form.getDescricaoLivre() == null || form.getDescricaoLivre().isBlank()) {
                throw new IllegalArgumentException("Descreva a entrada");
            }
        }
    }

    public EntradaControleForm paraFormEntrada(EntradaControle e) {
        EntradaControleForm f = new EntradaControleForm();
        f.setId(e.getId());
        f.setTipo(e.getTipo());
        f.setAlunoId(e.getAlunoId());
        f.setDescricaoLivre(e.getDescricaoLivre());
        f.setValor(e.getValor());
        f.setMeioPagamento(e.getMeioPagamento() != null ? e.getMeioPagamento() : MeioPagamentoEntrada.PIX);
        f.setMeioPagamentoOutro(e.getMeioPagamentoOutro());
        return f;
    }

    public List<SaidaControle> listarSaidas() {
        return saidaRepository.findAll();
    }

    public List<EntradaControle> listarEntradasManuais() {
        return entradaRepository.findAll();
    }

    public Optional<SaidaControle> buscarSaida(String id) {
        return saidaRepository.findById(id);
    }

    public Optional<EntradaControle> buscarEntrada(String id) {
        return entradaRepository.findById(id);
    }
}
