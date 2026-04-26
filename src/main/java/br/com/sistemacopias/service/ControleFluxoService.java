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
import br.com.sistemacopias.model.SaidaControle;
import br.com.sistemacopias.model.TipoEntradaControle;
import br.com.sistemacopias.repository.AlunoRepository;
import br.com.sistemacopias.repository.EntradaControleRepository;
import br.com.sistemacopias.repository.OrderRepository;
import br.com.sistemacopias.repository.SaidaControleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public ControleFluxoService(
            SaidaControleRepository saidaRepository,
            EntradaControleRepository entradaRepository,
            OrderRepository orderRepository,
            AlunoRepository alunoRepository) {
        this.saidaRepository = saidaRepository;
        this.entradaRepository = entradaRepository;
        this.orderRepository = orderRepository;
        this.alunoRepository = alunoRepository;
    }

    public FluxoDashboardDto montarDashboard(YearMonth mes) {
        FluxoDashboardDto dto = new FluxoDashboardDto();
        dto.setMesReferencia(mes);

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

        BigDecimal entMan = entradaRepository.findAll().stream()
                .filter(e -> e.getDataHoraRegistro() != null && YearMonth.from(e.getDataHoraRegistro()).equals(mes))
                .map(EntradaControle::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalEntradasManuaisMes(entMan);

        BigDecimal entPed = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && YearMonth.from(o.getCreatedAt()).equals(mes))
                .map(OrderRecord::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalEntradasPedidosMes(entPed);

        Map<String, String> nomesAlunos = alunoRepository.findAll().stream()
                .collect(Collectors.toMap(Aluno::getId, Aluno::getNomeCompleto, (a, b) -> a));

        List<MovimentoFluxoLinha> linhas = new ArrayList<>();

        for (SaidaControle s : saidaRepository.findAll()) {
            if (s.getDataCompraOuSaida() == null
                    || s.getDataCompraOuSaida().isBefore(ini)
                    || s.getDataCompraOuSaida().isAfter(fim)) {
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
            String desc = descricaoEntrada(e, nomesAlunos);
            String meio = e.getMeioPagamento() != null ? e.getMeioPagamentoResumo() : "-";
            linhas.add(new MovimentoFluxoLinha(
                    e.getId(),
                    e.getDataHoraRegistro(),
                    false,
                    desc,
                    e.getValor(),
                    "Entrada manual | Meio: " + meio));
        }

        for (OrderRecord o : orderRepository.findAll()) {
            if (o.getCreatedAt() == null || !YearMonth.from(o.getCreatedAt()).equals(mes)) {
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
        saidaRepository.update(existente);
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
        validarEntrada(form);
        EntradaControle existente = entradaRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Entrada nao encontrada"));
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
        entradaRepository.update(existente);
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
