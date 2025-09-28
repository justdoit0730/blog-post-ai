const tableDiv = document.getElementById("tableDiv");
let aiTemplate = [];

try {
    aiTemplate = JSON.parse(tableDiv.getAttribute("data-template"));
} catch(e) {
    console.error("템플릿 파싱 에러:", e);
}

const selectedTagBtn = document.getElementById("selectedTagBtn");
const tagList = document.getElementById("tagList");
const subjectInput = document.getElementById("subject");
const promptTextarea = document.getElementById("prompt");

// 1. isUsed = true 항목 찾기 (상단 버튼 표시용)
const usedItem = aiTemplate.find(item => item.isUsed);
if (usedItem) {
    selectedTagBtn.querySelector("span").textContent = usedItem.tag;
    subjectInput.value = usedItem.subject;
    promptTextarea.value = usedItem.prompt;
}

// 2. 모든 항목 리스트로 생성 (각 항목마다 <li>)
const allItems = [...aiTemplate].sort((a,b) => a.no - b.no);

tagList.innerHTML = ""; // 기존 내용 초기화

allItems.forEach(item => {
    const li = document.createElement("li");
    li.className = "selectset-item";

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "selectset-link btn";
    btn.dataset.value = item.tag;
    btn.innerHTML = `<span>${item.tag}</span>`;

    btn.addEventListener("click", () => {
        selectedTagBtn.querySelector("span").textContent = item.tag;
        subjectInput.value = item.subject;
        promptTextarea.value = item.prompt;
    });

    li.appendChild(btn);
    tagList.appendChild(li);
});
