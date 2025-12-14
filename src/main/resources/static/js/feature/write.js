document.getElementById('writeBtn').addEventListener('click', function() {
    const spinner = document.getElementById('spinner');
    const subject = document.getElementById("subject").value.trim();
    const prompt = document.getElementById("prompt").value.trim();
    const imageInput = document.getElementById("images");
    const files = imageInput.files;

    if (!subject) {
        alert("주제를 작성해주세요.");
        return;
    }

    const invalidFiles = [];
    for (let i = 0; i < files.length; i++) {
        if (!files[i].type.startsWith("image/")) {
            invalidFiles.push(files[i].name);
        }
    }

    if (invalidFiles.length > 0) {
        alert("허용되지 않는 파일 형식이 있습니다: " + invalidFiles.join(", "));
        return;
    }

    const images = [];
    const readers = [];

    spinner.style.visibility = 'visible';

    // FormData 객체 생성
    const formData = new FormData();
    formData.append("subject", subject);
    formData.append("prompt", prompt);
    for (let i = 0; i < files.length; i++) {
        formData.append("images", files[i]);
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/feature/write', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
            // 'Content-Type': 'application/json' 제거 — FormData는 브라우저가 자동 설정
        },
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(result => {
        // 기존 처리 로직 그대로
        const titleElement = document.getElementById("title");
        titleElement.textContent = result.title;

        let contentHtml = result.content;
        if (result.images && result.images.length > 0) {
            result.images.forEach((imgUrl, index) => {
                const photoTag = `[사진${index + 1}]`;
                const imgHtml = `<br><img src="${imgUrl}" style="max-width:100%;"><br>`;
                contentHtml = contentHtml.replace(photoTag, imgHtml);
            });
        }

        contentHtml = contentHtml.replace(/\r\n/g, "\n").replace(/\n/g, "<br>");
        document.getElementById("content").innerHTML = contentHtml;

        const imgUrlsElement = document.getElementById("imgUrls");
        if (result.images && result.images.length > 0) {
            imgUrlsElement.setAttribute("data-value", JSON.stringify(result.images));
        } else {
            imgUrlsElement.removeAttribute("data-value");
        }

        spinner.style.visibility = 'hidden';
        document.getElementById("writeSaveDiv").style.display = "block";
    })
    .catch(err => {
        alert("이미지 처리 중 오류가 발생했습니다. 사진 용량을 줄여 주세요.");
        spinner.style.visibility = 'hidden';
    });
});
