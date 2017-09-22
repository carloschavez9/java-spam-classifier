# Java SMS spam classification

Java NLP project for SMS spam classification. Uses Mallet for Natural Language Processing and Classification. Training and Testing is done using Naive Bayes and K fold cross validation.

## Arguments

- -t: runs in training mode
- -f: training file name
- -r: Indicates what to remove from tokens. 1 removes, 0 doesn't. [EMAIL, URL, NON_ALPHA, STOP_WORDS]. ie: [0,0,1,1]. Default [0,0,0,0]
- -m: text to evaluate

Results for training using default values:

    [main] INFO main.UtilsMallet - 10 fold cross validation
    [main] INFO main.UtilsMallet - Fold 1, Accuracy 0.98384
    [main] INFO main.UtilsMallet - Fold 2, Accuracy 0.98208
    [main] INFO main.UtilsMallet - Fold 3, Accuracy 0.97846
    [main] INFO main.UtilsMallet - Fold 4, Accuracy 0.97849
    [main] INFO main.UtilsMallet - Fold 5, Accuracy 0.96948
    [main] INFO main.UtilsMallet - Fold 6, Accuracy 0.98025
    [main] INFO main.UtilsMallet - Fold 7, Accuracy 0.98746
    [main] INFO main.UtilsMallet - Fold 8, Accuracy 0.98384
    [main] INFO main.UtilsMallet - Fold 9, Accuracy 0.99104
    [main] INFO main.UtilsMallet - Fold 10, Accuracy 0.99282
    [main] INFO main.UtilsMallet - Mean Accuracy 0.98278 

## Dataset

SMS Spam Collection

- [Web link](http://www.dt.fee.unicamp.br/~tiago/smsspamcollection)
- [UCI Machine Learning Repository](https://archive.ics.uci.edu/ml/datasets/SMS+Spam+Collection)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
