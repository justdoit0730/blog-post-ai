const tableIdDiv = document.getElementById("tableIdDiv");
let cafeIdTemplate = [];
try {
    const templateStr = tableIdDiv.getAttribute("data-template");
    if (templateStr) {
        cafeIdTemplate = JSON.parse(templateStr) || [];
    }
} catch (e) {
    console.error("템플릿 파싱 에러:", e);
    cafeIdTemplate = [];
}

function updateRowNumbers() {
    [...tableIdBody.rows].forEach((row, index) => {
        row.cells[1].textContent = index + 1;
    });
}

function createEditableCell(row, placeholder = "") {
    const cell = row.insertCell();
    cell.contentEditable = "true";
    cell.textContent = placeholder;
    cell.title = placeholder;
    cell.addEventListener("input", () => {
        cell.title = cell.textContent;
    });
    return cell;
}

function addRow(item = {}) {
    const row = tableIdBody.insertRow();

    const useCell = row.insertCell(0);
    const radio = document.createElement("input");
    radio.type = "radio";
    radio.name = "useIdRow";
    radio.checked = item?.isUsed || false;
    if (radio.checked) row.classList.add("active");

    useCell.addEventListener("click", () => {
        radio.checked = true;
        [...tableIdBody.rows].forEach(r => r.classList.remove("active"));
        row.classList.add("active");
    });

    useCell.appendChild(radio);

    // No
    const noCell = row.insertCell(1);
    noCell.textContent = item?.no ?? tableIdBody.rows.length;

    // 카페 이름, 카페 ID
    createEditableCell(row, item?.cafeName ?? "");
    createEditableCell(row, item?.cafeId ?? "");

    // 삭제 버튼
    const deleteCell = row.insertCell();
    const deleteBtn = document.createElement("button");
    deleteBtn.type = "button";
    deleteBtn.style.background = "white";
    deleteBtn.innerHTML = `<img src="/resources/profile/delete.svg" style="width:2rem;object-fit:contain;" />`;
    deleteBtn.addEventListener("click", () => {
        row.remove();
        updateRowNumbers();
        // 삭제 후 첫 행 체크 유지
        if (tableIdBody.rows.length > 0) {
            const firstRowRadio = tableIdBody.rows[0].cells[0].querySelector("input[type='radio']");
            firstRowRadio.checked = true;
            tableIdBody.rows[0].classList.add("active");
        }
    });
    deleteCell.appendChild(deleteBtn);
}

cafeIdTemplate.forEach(item => addRow(item));
updateRowNumbers();
