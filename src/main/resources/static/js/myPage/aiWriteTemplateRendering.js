const tableDiv = document.getElementById("tableDiv");
const tableBody = document.querySelector("#dynamicTable tbody");
const addRowBtn = document.getElementById("addRowBtn");

let aiTemplate = [];
try {
    const templateStr = tableDiv.getAttribute("data-template"); 
    aiTemplate = JSON.parse(templateStr);
} catch (e) {
    console.error("템플릿 파싱 에러:", e);
}

function updateRowNumbers() {
    [...tableBody.rows].forEach((row, index) => {
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
    const row = tableBody.insertRow();

    const useCell = row.insertCell(0);
    const radio = document.createElement("input");
    radio.type = "radio";
    radio.name = "useRow";
    radio.checked = item?.isUsed || false;
    if (radio.checked) row.classList.add("active");

    useCell.addEventListener("click", () => {
        radio.checked = true;
        [...tableBody.rows].forEach(r => r.classList.remove("active"));
        row.classList.add("active");
    });

    useCell.appendChild(radio);

    // No
    const noCell = row.insertCell(1);
    noCell.textContent = item?.no ?? tableBody.rows.length;

    // 태그, 주제, 요청 사항
    createEditableCell(row, item?.tag ?? "");
    createEditableCell(row, item?.subject ?? "");
    createEditableCell(row, item?.prompt ?? "");

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
        if (tableBody.rows.length > 0) {
            const firstRowRadio = tableBody.rows[0].cells[0].querySelector("input[type='radio']");
            firstRowRadio.checked = true;
            tableBody.rows[0].classList.add("active");
        }
    });
    deleteCell.appendChild(deleteBtn);
}

aiTemplate.forEach(item => addRow(item));
updateRowNumbers();

addRowBtn.addEventListener("click", () => addRow());