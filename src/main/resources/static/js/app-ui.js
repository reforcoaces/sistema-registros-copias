/**
 * Botao Voltar inferior: historico ou fallback por modulo.
 */
window.voltarApp = function () {
    if (window.history.length > 1) {
        window.history.back();
        return;
    }
    var p = window.location.pathname || "";
    if (p.indexOf("/controle-fluxo") === 0) {
        window.location.href = "/controle-fluxo/dashboard";
    } else if (p.indexOf("/reforco") === 0) {
        window.location.href = "/reforco/dashboard";
    } else if (p === "/escolher-sistema") {
        window.location.href = "/login";
    } else {
        window.location.href = "/";
    }
};
