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

    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const reader = new FileReader();

        readers.push(new Promise((resolve, reject) => {
            reader.onload = function(e) {
                images.push(e.target.result);
                resolve();
            };
            reader.onerror = reject;
        }));

        reader.readAsDataURL(file);
    }

    spinner.style.visibility = 'visible';
    Promise.all(readers).then(() => {
        const data = {
            subject: subject,
            prompt: prompt,
            images: images
        };

        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        fetch('/feature/write', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data),
            credentials: 'same-origin'
        })
        .then(response => response.json())
        .then(result => {
            const titleElement = document.getElementById("title");
            titleElement.textContent = result.title;

            let contentHtml = result.content;

            if (result.images && result.images.length > 0) {
                result.images.forEach((imgDataUrl, index) => {
                    const photoTag = `[사진${index + 1}]`;
                    const imgHtml = `<br><img src="${imgDataUrl}" style="max-width:100%;"><br>`;
                    contentHtml = contentHtml.replace(photoTag, imgHtml);
                });
            }

            contentHtml = contentHtml.replace(/\r\n/g, "\n").replace(/\n/g, "<br>");

            const contentElement = document.getElementById("content");
            contentElement.innerHTML = contentHtml;

            const imgUrlsElement = document.getElementById("imgUrls");
            if (result.images && result.images.length > 0) {
                imgUrlsElement.setAttribute("data-value", JSON.stringify(result.images));
            } else {
                imgUrlsElement.removeAttribute("data-value");
            }
            const imgUrlS = document.getElementById("imgUrls").dataset.value;

            spinner.style.visibility = 'hidden';
            const writeSaveDiv = document.getElementById("writeSaveDiv");
            writeSaveDiv.style.display = "block";
        })

    }).catch(err => {
        alert("이미지 처리 중 오류가 발생했습니다.");
        spinner.style.visibility = 'hidden';
    });
});
