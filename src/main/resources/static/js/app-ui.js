(function () {
    function initPasswordToggle() {
        var btn = document.getElementById("toggle-password");
        var input = document.getElementById("password");
        if (!btn || !input || btn.getAttribute("data-pw-init") === "1") {
            return;
        }
        btn.setAttribute("data-pw-init", "1");
        var iconShow = btn.querySelector(".password-field__icon--show");
        var iconHide = btn.querySelector(".password-field__icon--hide");
        btn.addEventListener("click", function (ev) {
            ev.preventDefault();
            ev.stopPropagation();
            var reveal = input.type === "password";
            input.type = reveal ? "text" : "password";
            var visible = input.type === "text";
            btn.setAttribute("aria-pressed", visible ? "true" : "false");
            btn.setAttribute("aria-label", visible ? "Ocultar senha" : "Mostrar senha");
            btn.setAttribute("title", visible ? "Ocultar senha" : "Mostrar senha");
            if (iconShow) {
                iconShow.hidden = visible;
            }
            if (iconHide) {
                iconHide.hidden = !visible;
            }
        });
    }
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initPasswordToggle);
    } else {
        initPasswordToggle();
    }
})();

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
