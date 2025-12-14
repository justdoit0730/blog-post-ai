document.getElementById('cafeAiPostBtn').addEventListener('click', function() {
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

    const formData = new FormData();
    formData.append("subject", subject);
    formData.append("prompt", prompt);

    for (let i = 0; i < files.length; i++) {
        formData.append("images", files[i]);
    }

    spinner.style.visibility = 'visible';

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch('/feature/post', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        body: formData,
        credentials: 'same-origin'
    })
    .then(async (response) => {
        if (!response.ok) {
            const text = await response.text();
            console.error(text);
            throw new Error("서버 오류 발생");
        }
        return response.json();
    })
    .then(result => {
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

        window.editor = new toastui.Editor({
            el: document.querySelector('#editor'),
            height: '70rem',
            initialEditType: 'wysiwyg',
            previewStyle: 'vertical',
            initialValue: contentHtml
        });

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
        console.error(err);
        alert("요청 중 오류가 발생했습니다.");
        spinner.style.visibility = 'hidden';
    });
});