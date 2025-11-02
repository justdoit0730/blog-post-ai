document.getElementById('writeSave').addEventListener('click', function() {
    const title = document.getElementById("title").textContent.trim();
    const subject = document.getElementById("subject").value.trim();
    const prompt = document.getElementById("prompt").value.trim();
    const imgUrlsData = document.getElementById("imgUrls").dataset.value;
    const fullContent = document.getElementById("content").outerHTML;

    console.log(fullContent);

    const imgUrlS = imgUrlsData ? JSON.parse(imgUrlsData) : [];

    var data = {
        title: title,
        subject: subject,
        prompt: prompt,
        imgUrlS: imgUrlS,
        fullContent: fullContent
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/write/save', {
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
                alert("글이 저장 되었습니다.");
                window.location.href = "/feature/write";
            } else {
                alert("글 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
        })
        .catch(err => {

            alert("글 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        });
});
