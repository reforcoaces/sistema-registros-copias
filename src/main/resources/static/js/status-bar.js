/**
 * Barra superior: data em portugues, hora com segundos (atualizacao por segundo),
 * temperatura via geolocalizacao + Open-Meteo (sem chave API).
 */
(function () {
    var lineEl = document.getElementById("app-status-line");
    var tempEl = document.getElementById("app-status-temp");
    if (!lineEl || !tempEl) {
        return;
    }

    function pad(n) {
        return n < 10 ? "0" + n : String(n);
    }

    function tick() {
        var d = new Date();
        var datePart = new Intl.DateTimeFormat("pt-BR", {
            weekday: "long",
            day: "numeric",
            month: "long",
            year: "numeric",
        }).format(d);
        var cap = datePart.charAt(0).toUpperCase() + datePart.slice(1);
        var timeStr = pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds());
        lineEl.textContent = cap + " — " + timeStr;
    }

    tick();
    setInterval(tick, 1000);

    function setTemp(text, title) {
        tempEl.textContent = text;
        if (title) {
            tempEl.setAttribute("title", title);
        }
    }

    function fetchTemp(lat, lon) {
        var url =
            "https://api.open-meteo.com/v1/forecast?latitude=" +
            encodeURIComponent(lat) +
            "&longitude=" +
            encodeURIComponent(lon) +
            "&current_weather=true";
        fetch(url)
            .then(function (r) {
                return r.json();
            })
            .then(function (j) {
                var t = j && j.current_weather && typeof j.current_weather.temperature === "number"
                    ? j.current_weather.temperature
                    : null;
                if (t !== null) {
                    setTemp(Math.round(t) + "\u00b0C", "Temperatura no local (Open-Meteo)");
                } else {
                    setTemp("\u2014", "Dados meteorologicos indisponiveis");
                }
            })
            .catch(function () {
                setTemp("\u2014", "Nao foi possivel obter a temperatura");
            });
    }

    if (!navigator.geolocation) {
        setTemp("\u2014", "Geolocalizacao nao suportada neste navegador");
        return;
    }

    setTemp("…", "A obter localizacao…");

    navigator.geolocation.getCurrentPosition(
        function (pos) {
            fetchTemp(pos.coords.latitude, pos.coords.longitude);
        },
        function () {
            setTemp("\u2014", "Permita a localizacao para ver a temperatura no local");
        },
        { enableHighAccuracy: false, timeout: 18000, maximumAge: 600000 }
    );
})();
