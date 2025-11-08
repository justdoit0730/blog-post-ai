document.getElementById('postBtn').addEventListener('click', async function() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    const cafeName = usedItem.cafeName;
    const cafeId = selectedCafeIdBtn.dataset.cafeId;

    const cafeBoardTag = usedTagItem.tag;
    const cafeMenuId = selectedTagBtn.dataset.cafeMenuId;

    const title = document.getElementById("subject").value.trim();
    if (title === "") {
        alert("제목을 입력하세요.");
        return;
    }
    let contentHtml = "";

    try {
        contentHtml = await replaceAndRender();
        console.log("이미지 변환 및 HTML 교체 완료");
    } catch (error) {
        console.error("replaceAndRender 실행 중 오류:", error);
    }

    if (contentHtml === "" || contentHtml === "<p></p>") {
        alert("내용을 입력하세요.");
        return;
    }

    var data = {
        cafeName: cafeName,
        cafeId: cafeId,

        cafeBoardTag: cafeBoardTag,
        cafeBoardId: cafeMenuId,

        title: title,
        contentHtml: contentHtml
    };

    fetch('/cafe/post', {
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
            alert("카페에 글이 성공적으로 게시 되었습니다");
            window.location.href = "/cafe/post";
        } else {
            alert("카페 글 게시 요청 중 오류가 발생했습니다. API 설정 확인 및 관리자 문의 바랍니다.");
        }
    })
    .catch(err => {
        console.error(err);
        alert("포스팅 템플릿이 저장 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    });
});

async function replaceAndRender() {
    let html = editor.getHTML();

    if (html === "" || html === "<p></p>") {
        alert("내용을 입력하세요.");
        return;
    }

    const base64Images = [...html.matchAll(/<img[^>]+src=["'](data:image\/[^"']+)["']/g)]
                        .map(m => m[1]);

    let replacedHtml = html;

    if (base64Images.length > 0) {
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch('/cafe/uploadImages', {
          method: 'POST',
          headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ base64Images })
        });

        const urls = await response.json();

        base64Images.forEach((base64, i) => {
            replacedHtml = replacedHtml.replace(base64, urls[i]);
        });
    }

    return replacedHtml;
}