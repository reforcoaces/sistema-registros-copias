(function () {
    function formatBRL(n) {
        return "R$ " + n.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function parseQty(v) {
        var n = parseInt(v, 10);
        return isNaN(n) || n < 1 ? 0 : n;
    }

    function initOrderFormTotal() {
        var form = document.getElementById("order-form");
        if (!form) return;

        var totalEl = document.getElementById("total-atendimento");
        if (!totalEl) return;

        var prices;
        try {
            prices = JSON.parse(form.getAttribute("data-product-prices") || "{}");
        } catch (e) {
            prices = {};
        }

        function recalc() {
            var sum = 0;
            var rows = form.querySelectorAll(".order-row");
            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var sel = row.querySelector("select[name$='.productType']");
                var qIn = row.querySelector("input[name$='.quantity']");
                if (!sel || !qIn) continue;
                var code = sel.value;
                var qty = parseQty(qIn.value);
                if (!code || qty < 1) continue;
                var unit = parseFloat(prices[code]);
                if (!isFinite(unit)) continue;
                sum += unit * qty;
            }
            totalEl.textContent = formatBRL(sum);
        }

        form.addEventListener("change", recalc);
        form.addEventListener("input", recalc);
        recalc();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initOrderFormTotal);
    } else {
        initOrderFormTotal();
    }
})();
