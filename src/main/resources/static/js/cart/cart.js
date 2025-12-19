document.addEventListener("DOMContentLoaded", () => {
    const checkAll = document.getElementById("checkAll");
    const itemChecks = () => Array.from(document.querySelectorAll(".itemCheck"));

    const totalText = document.getElementById("totalPriceText");
    const discountText = document.getElementById("discountText");
    const finalText = document.getElementById("finalPriceText");

    function formatWon(n) {
        return n.toLocaleString("ko-KR") + "원";
    }

    function calcSelectedTotal() {
        return itemChecks()
            .filter(chk => chk.checked)
            .reduce((sum, chk) => sum + Number(chk.dataset.price || 0), 0);
    }

    function renderPrice() {
        const total = calcSelectedTotal();
        const discount = 0;
        const final = total - discount;

        if (totalText) totalText.textContent = formatWon(total);
        if (discountText) discountText.textContent = "-" + formatWon(discount);
        if (finalText) finalText.textContent = formatWon(final);

        const all = itemChecks();
        if (checkAll && all.length > 0) {
            checkAll.checked = all.every(chk => chk.checked);
        }
    }

    if (checkAll) {
        checkAll.addEventListener("change", () => {
            itemChecks().forEach(chk => (chk.checked = checkAll.checked));
            renderPrice();
        });
    }

    itemChecks().forEach(chk => chk.addEventListener("change", renderPrice));

    renderPrice();
});