<?php

class VideoPlayer {
    protected $video;
    protected $isPlaying = false;

    public function __construct(string $video) {
        $this->video = $video;
    }

    public function play(): string {
        $this->isPlaying = true;
        return "▶ Відтворення відео: " . $this->video;
    }

}

// Використання:
$player = new VideoPlayer("film.mp4");
echo $player->play() . "\n";  // ▶ Відтворення відео: film.mp4
?>



<?php

// Базовий інтерфейс для відеоплеєра
interface IVideoPlayer {
    public function play(): string;
}

// Основний клас відеоплеєра
class BasicVideoPlayer implements IVideoPlayer {
    protected $video;
    protected $isPlaying = false;

    public function __construct(string $video) {
        $this->video = $video;
    }

    public function play(): string {
        $this->isPlaying = true;
        return "Відтворення відео: " . $this->video;
    }

}

// Декоратор для додавання субтитрів
class SubtitlesDecorator implements IVideoPlayer {
    protected IVideoPlayer $player;
    protected string $subtitle;

    public function __construct(IVideoPlayer $player, string $subtitle) {
        $this->player = $player;
        $this->subtitle = $subtitle;
    }

    public function play(): string {
        return $this->player->play() . " + Субтитри: " . $this->subtitle;
    }
}

// Декоратор для зміни мови аудіодоріжки
class AudioLanguageDecorator implements IVideoPlayer {
    protected IVideoPlayer $player;
    protected string $language;

    public function __construct(IVideoPlayer $player, string $language) {
        $this->player = $player;
        $this->language = $language;
    }

    public function play(): string {
        return $this->player->play() . " + Аудіо: " . $this->language;
    }
}

// Декоратор для зміни якості відео
class QualityDecorator implements IVideoPlayer {
    protected IVideoPlayer $player;
    protected string $quality;

    public function __construct(IVideoPlayer $player, string $quality) {
        $this->player = $player;
        $this->quality = $quality;
    }

    public function play(): string {
        return $this->player->play() . " + Якість: " . $this->quality;
    }
}

// Використання
$video = new BasicVideoPlayer("movie.mp4");

// Додаємо субтитри
$videoWithSubtitles = new SubtitlesDecorator($video, "Ukrainian");

// Додаємо зміну мови аудіодоріжки
$videoWithAudio = new AudioLanguageDecorator($videoWithSubtitles, "English Dub");

// Додаємо зміну якості відео
$videoWithQuality = new QualityDecorator($videoWithAudio, "1080p");

echo $videoWithQuality->play();
// Виведе: Відтворення відео: movie.mp4 + Субтитри: Ukrainian + Аудіо: English Dub + Якість: 1080p

?>
