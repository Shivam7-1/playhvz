echo 'Running creategame test!'
python creategame.py fake $1
echo 'Running joingame test!'
python joingame.py fake $1
echo 'Running infect test!'
python infect.py fake $1
echo 'Running declare test!'
python declare.py fake $1
