document.addEventListener('DOMContentLoaded', () => {
    const track = document.querySelector('.banner-track');
    const slides = document.querySelectorAll('.banner-item');
    const navButtons = document.querySelectorAll('.banner-nav');
    const progressItems = document.querySelectorAll('.banner-progress span');


    let currentIndex = 0;
    const slideCount = slides.length;


    //슬라이드 이동
    function moveSlide(index){
        track.style.transform = `translateX(-${index * 100}%)`;

        progressItems.forEach((item, i) => {
            item.classList.toggle('active', i === index);
        });
    }

    //이전, 다음 버튼
    function prevSlide(){
        currentIndex = (currentIndex - 1 + slideCount) % slideCount;
        moveSlide(currentIndex);
    }
    function nextSlide(){
        currentIndex = (currentIndex + 1) % slideCount;
        moveSlide(currentIndex);
    }

    //네비게이션 클릭 시 실행
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const dir = btn.dataset.dir;
            if (dir === 'next') {
                nextSlide();
            } else {
                prevSlide();
            }
        });
    });

})