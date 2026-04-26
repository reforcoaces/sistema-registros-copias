/**
 * Mascara pt-BR para valores monetarios (centavos digitados da direita para a esquerda).
 * Envolver o input visivel e um hidden com name="valor" (th:field) na .input-moeda-wrap.
 */
(function () {
    function formatBr(num) {
        return new Intl.NumberFormat("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num);
    }

    function syncHidden(vis, hid) {
        var raw = (vis.value || "").replace(/\D/g, "");
        if (!raw) {
            hid.value = "";
            return;
        }
        var n = parseInt(raw, 10) / 100;
        hid.value = n.toFixed(2);
    }

    function syncVisible(vis, hid) {
        var v = parseFloat(hid.value);
        if (isFinite(v) && v >= 0) {
            vis.value = formatBr(v);
        } else {
            vis.value = "";
        }
    }

    function bindWrap(wrap) {
        var vis = wrap.querySelector(".input-moeda-br");
        var hid = wrap.querySelector(".input-moeda-hidden");
        if (!vis || !hid) {
            return;
        }
        syncVisible(vis, hid);
        vis.addEventListener("input", function () {
            syncHidden(vis, hid);
            var raw = (vis.value || "").replace(/\D/g, "");
            if (raw) {
                vis.value = formatBr(parseInt(raw, 10) / 100);
            }
        });
        vis.addEventListener("blur", function () {
            syncHidden(vis, hid);
            var raw = (vis.value || "").replace(/\D/g, "");
            if (raw) {
                vis.value = formatBr(parseInt(raw, 10) / 100);
            } else {
                vis.value = "";
                hid.value = "";
            }
        });
    }

    function onSubmit(form) {
        form.querySelectorAll(".input-moeda-wrap").forEach(function (wrap) {
            var vis = wrap.querySelector(".input-moeda-br");
            var hid = wrap.querySelector(".input-moeda-hidden");
            if (vis && hid) {
                syncHidden(vis, hid);
            }
        });
    }

    function init() {
        document.querySelectorAll(".input-moeda-wrap").forEach(bindWrap);
        document.querySelectorAll("form").forEach(function (f) {
            f.addEventListener("submit", function () {
                onSubmit(f);
            });
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
