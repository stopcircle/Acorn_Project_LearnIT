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

        totalText.textContent = formatWon(total);
        discountText.textContent = "-" + formatWon(discount);
        finalText.textContent = formatWon(final);

        checkAll.checked = itemChecks().every(chk => chk.checked);
    }

    checkAll.addEventListener("change", () => {
        itemChecks().forEach(chk => chk.checked = checkAll.checked);
        renderPrice();
    });

    itemChecks().forEach(chk =>
        chk.addEventListener("change", renderPrice)
    );

    renderPrice();

    // 결제 버튼
    document.querySelector(".summary-btn").addEventListener("click", () => {

        const selected = itemChecks().filter(chk => chk.checked);

        if (selected.length === 0) {
            alert("결제할 강의를 선택하세요.");
            return;
        }

        const courseIds = selected.map(chk =>
            chk.closest(".cart-item")
                .querySelector("input[name='courseId']").value
        );

        //결제 페이지로 이동
        location.href = "/payment?courseIds=" + courseIds.join(",");
    });
});
