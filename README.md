
Да махна един два реда

Да се имплементира sentiment analyzer за филмови отзиви, който автоматично ще определя степента на позитивност на даден отзив в свободен текст.

Например, нашият алгоритъм би определил отзива 

*"Dire disappointment: dull and unamusing freakshow"*

като твърдо негативен, докато отзивът

*"Immersive ecstasy: energizing artwork!"*

ще се класифицира като еднозначно позитивен.

Данните, от които ще "учи" нашият алгоритъм, са множество от 8,529 филмови отзива (ревюта), за които отношението на автора е било оценено от човек по скала от 0 до 4 със следната семантика:

| рейтинг | семантика         |
| ------- | ----------------- |
| 0       | negative          |
| 1       | somewhat negative |
| 2       | neutral           |
| 3       | somewhat positive |
| 4       | positive          |

Ще използваме data set от сайта [Rotten Tomatoes](https://www.rottentomatoes.com/), използван наскоро за престижния [Кaggle machine learning competition](https://www.kaggle.com/c/sentiment-analysis-on-movie-reviews).

Данните са налични в текстовия файл [reviews.txt](https://github.com/fmi/java-course/tree/master/homeworks/02-movie-review-sentiment-analyzer/resources/reviews.txt), като всеки ред от файла започва с рейтинг, следван от интервал и текста на отзива, например:

```
4 The performances are an absolute joy .
```

Имайте предвид, че е напълно очаквано в подобен real-life data set да има typos, жаргонни или направо несъществуващи думи. 

Има обаче едно множество от често срещани в свободен текст думи, които носят твърде малко семантика: определителни членове, местоимения, предлози, съюзи и т.н. Такива думи се наричат *stopwords* и много алгоритми, свързани с обработка на естествен език (NLP, natural language processing), ги игнорират - т.е. премахват ги от съответните входни текстове, защото внасят "шум", т.е. намаляват качеството на резултата. Няма еднозначна дефиниция (или речник) коя дума е stopword в даден език. В нашия алгоритъм ще ползваме списъка от 174 stopwords в английския език, записани по една на ред в текстовия файл [stopwords.txt](https://github.com/fmi/java-course/tree/master/homeworks/02-movie-review-sentiment-analyzer/resources/stopwords.txt), който сме заимствали от сайта [ranks.nl](https://www.ranks.nl/stopwords).

Алгоритъмът, който трябва да имплементирате, е следният:

Обучение:

1. Изчитат се отзивите в [reviews.txt](https://github.com/fmi/java-course/tree/master/homeworks/02-movie-review-sentiment-analyzer/resources/reviews.txt)
2. Изчислява се sentiment score на всяка дума като средно аритметично (без закръгляване) на всички рейтинги, в които участва дадената дума. Дума е последователност от малки и главни латински букви и цифри с дължина поне един символ. Думите са case-insensitive, т.е. "Movie", "movie" и "movIE" се третират като една и съща дума. Един отзив се състои от думи, разделени с разделители: интервали, табулации и препинателни знаци - въобще всеки символ, който не е буква или цифра. Stopwords се игнорират, т.е. не се взимат под внимание.

Разпознаване:

1. По даден текст на отзив се изчислява неговият sentiment score като средно аритметично (без закръгляване) на sentiment scores на всяка дума в отзива. Дефиницията на дума е същата като по-горе, и stopwords отново се игнорират. Игнорират се също всички (непознати) думи, за които алгоритъмът не е обучен, т.е. не се срещат нито веднъж в [reviews.txt](https://github.com/fmi/java-course/tree/master/homeworks/02-movie-review-sentiment-analyzer/resources/reviews.txt). Sentiment score на отзив, в който не се съдържа нито една дума с известен sentiment score (състои се само от непознати думи и/или stopwords), се приема за -1.0 (unknown).

В пакет `bg.sofia.uni.fmi.mjt.sentiment` създайте клас `MovieReviewSentimentAnalyzer`, който имплементира интерфейса `SentimentAnalyzer` по-долу и има конструктор

`public MovieReviewSentimentAnalyzer(InputStream stopwordsInput, InputStream reviewsInput,OutputStream reviewsOutput)`

Kато трите параметъра са съответно поток за четене на [stopwords.txt](https://github.com/fmi/java-course/tree/master/homeworks/02-movie-review-sentiment-analyzer/resources/stopwords.txt), поток за четене на [reviews.txt](https://github.com/fmi/java-course/tree/master/homeworks/02-movie-review-sentiment-analyzer/resources/reviews.txt) и поток за писане в [reviews.txt](https://github.com/fmi/java-course/tree/master/homeworks/02-movie-review-sentiment-analyzer/resources/reviews.txt).

Нашият data set от ревюта трябва да може да се разширява. Това ще допринесе за допълнителна точност при меренето на sentiment във времето. Точно това е идеята и на `outputStream`-a - чрез него ще добавяме нови ревюта към `reviews.txt`.

Методът `append` ни дава възможност да усъвършенстваме нашия sentiment analyzer. При добавяне на нови ревюта и оценки, преизчисляваме sentiment-a на думите от ревюто. Уверете се, че след `append`-a добавяте и `System.lineSeparator()`.
