// Cafe ID Template
const cafeIdTemplateDiv = document.getElementById("cafeIdTemplate");
let cafeIdTemplate = [];

try {
    cafeIdTemplate = JSON.parse(cafeIdTemplateDiv.getAttribute("data-template"));
} catch(e) {
    console.error("템플릿 파싱 에러:", e);
}

const selectedCafeIdBtn = document.getElementById("selectedCafeIdBtn");
const cafeNameList = document.getElementById("cafeNameList");

const usedItem = cafeIdTemplate.find(item => item.isUsed);
if (usedItem) {
    selectedCafeIdBtn.querySelector("span").textContent = usedItem.cafeName;
    selectedCafeIdBtn.dataset.cafeId = usedItem.cafeId;
}

const allItems = [...cafeIdTemplate].sort((a,b) => a.no - b.no);

cafeNameList.innerHTML = "";

allItems.forEach(item => {
    const li = document.createElement("li");
    li.className = "selectset-item";

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "selectset-link btn";
    btn.dataset.cafeId = item.cafeId;
    btn.innerHTML = `<span>${item.cafeName}</span>`;

    btn.addEventListener("click", () => {
        selectedCafeIdBtn.querySelector("span").textContent = item.cafeName;
        selectedCafeIdBtn.dataset.cafeId = item.cafeId;
    });

    li.appendChild(btn);
    cafeNameList.appendChild(li);
});

// Cafe Posting Template
const cafePostingTemplateDiv = document.getElementById("cafePostingTemplate");
let cafePostingTemplate = [];

try {
    cafePostingTemplate = JSON.parse(cafePostingTemplateDiv.getAttribute("data-template"));
} catch(e) {
    console.error("템플릿 파싱 에러:", e);
}

const selectedTagBtn = document.getElementById("selectedTagBtn");
const tagList = document.getElementById("tagList");

const usedTagItem = cafePostingTemplate.find(item => item.isUsed);
if (usedTagItem) {
    selectedTagBtn.querySelector("span").textContent = usedTagItem.tag;
    selectedTagBtn.dataset.cafeMenuId = usedTagItem.cafeMenuId;
}

const allPostItems = [...cafePostingTemplate].sort((a,b) => a.no - b.no);

tagList.innerHTML = "";

allPostItems.forEach(item => {
    const li = document.createElement("li");
    li.className = "selectset-item";

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "selectset-link btn";
    btn.dataset.cafeMenuId = item.cafeMenuId;
    btn.innerHTML = `<span>${item.tag}</span>`;

    btn.addEventListener("click", () => {
        selectedTagBtn.querySelector("span").textContent = item.tag;
        selectedTagBtn.dataset.cafeMenuId = item.cafeMenuId;
    });

    li.appendChild(btn);
    tagList.appendChild(li);
});


function initPostingTemplate() {
    const cafeTemplateDiv = document.getElementById("cafePostingTemplate");

    const dataTemplate = cafeTemplateDiv?.getAttribute("data-template");
    if (!dataTemplate || dataTemplate.trim() === "") {
        return;
    }
    let cafePostTemplate = [];

    try {
        cafePostTemplate = JSON.parse(cafeTemplateDiv.getAttribute("data-template"));
    } catch(e) {
        console.error("템플릿 파싱 에러:", e);
    }

    const selectedTagBtn = document.getElementById("selectedTagBtn");
    const tagList = document.getElementById("tagList");
    const subjectInput = document.getElementById("subject");
    const promptTextarea = document.getElementById("prompt");

    const usedItem = cafePostTemplate.find(item => item.isUsed);
    if (usedItem) {
        selectedTagBtn.querySelector("span").textContent = usedItem.tag;
        subjectInput.value = usedItem.subject;
        promptTextarea.value = usedItem.prompt;
    }

    const allItems = [...cafePostTemplate].sort((a,b) => a.no - b.no);

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

initPostingTemplate();