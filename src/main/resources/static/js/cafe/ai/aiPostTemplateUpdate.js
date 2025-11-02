document.getElementById('aiPostTemplateUpdate').addEventListener('click', function() {
    const templateName = document.getElementById('templateName').textContent;
    const subject = document.getElementById("subject").value.trim();
    const prompt = document.getElementById("prompt").value.trim();

    var data = {
        tag: templateName,
        subject: subject,
        prompt: prompt
    };

    console.log(data);

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/cafe/ai/template/update', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
        credentials: 'same-origin'
    })
    .then(response => response.text())
    .then(result => {
        if (result === "T") {
            alert("템플릿이 저장 되었습니다");
        } else {
            alert("템플릿 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("템플릿 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });
});
