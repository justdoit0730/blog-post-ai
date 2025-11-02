function initTemplate() {
    const tableDiv = document.getElementById("tableDiv");

    const dataTemplate = tableDiv?.getAttribute("data-template");
    if (!dataTemplate || dataTemplate.trim() === "") {
        return;
    }
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

    const usedItem = aiTemplate.find(item => item.isUsed);
    if (usedItem) {
        selectedTagBtn.querySelector("span").textContent = usedItem.tag;
        subjectInput.value = usedItem.subject;
        promptTextarea.value = usedItem.prompt;
    }

    const allItems = [...aiTemplate].sort((a,b) => a.no - b.no);

    tagList.innerHTML = "";

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
}

initTemplate();