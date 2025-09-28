const tableDiv = document.getElementById("tableDiv");
let cafePostingTemplate = [];
try {  
    const postingTemplateStr = tableDiv.getAttribute("data-template");
    if (postingTemplateStr) {
        cafePostingTemplate = JSON.parse(postingTemplateStr) || [];
    }
} catch (e) {
    console.error("템플릿 파싱 에러:", e);
    cafePostingTemplate = [];
}

function updatePostingRowNumbers() {
    [...tableBody.rows].forEach((row, index) => {
        row.cells[1].textContent = index + 1;
    });
}

function addPostingRow(item = {}) {
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

    // 태그, 게시판ID, 주제, 요청사항
    createEditableCell(row, item?.tag ?? "");
    createEditableCell(row, item?.cafeMenuId ?? "");
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
        updatePostingRowNumbers();
        // 삭제 후 첫 행 체크 유지
        if (tableBody.rows.length > 0) {
            const firstRowRadio = tableBody.rows[0].cells[0].querySelector("input[type='radio']");
            firstRowRadio.checked = true;
            tableBody.rows[0].classList.add("active");
        }
    });
    deleteCell.appendChild(deleteBtn);
}

cafePostingTemplate.forEach(item => addPostingRow(item));
updatePostingRowNumbers();
