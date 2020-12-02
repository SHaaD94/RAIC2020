# repeat n times
#       start runner
#       connect clients
#       wait for runner finishes
#       read results
# show stats
# fail if won less than threshold

import sys
import subprocess
import json

current_version_path = '../target/aicup2020-jar-with-dependencies.jar'


class GameResult:
    def __init__(self, is_winner, is_crashed, seed):
        self.is_winner = is_winner
        self.is_crashed = is_crashed
        self.seed = seed
        pass

    def __str__(self):
        return f'GameResult(winner {self.is_winner}, crashed {self.is_crashed}, seed {self.seed})'


def parse_game_result(res_file):
    with open(res_file) as json_file:
        data = json.load(json_file)
        seed = data['seed']
        is_crashed = data['players'][0]['crashed']

        results = data['results']
        mine = int(results[0])

        is_winner = True

        for other_result in results[1:]:
            if mine < int(other_result):
                is_winner = False
                break

        return GameResult(is_winner, is_crashed, seed)


def run_games(folder, repeats):
    games = []
    players = []
    with open(f'{folder}/config.json') as json_file:
        players = json.load(json_file)['players']

    for i in range(repeats):
        print(f'Starting game number {i}')
        runner_process = subprocess.Popen(['./aicup2020',
                                           '--batch-mode',
                                           '--config', f'{folder}/config.json',
                                           '--save-results', f'{folder}/res.json'])

        for p in players:
            port = p['Tcp']['port']
            if p['Tcp'] is not None:
                _ = subprocess.Popen(['java',
                                      '-jar',
                                      f'{folder}/v.jar', '127.0.0.1', f'{port}'])

        runner_process.wait()

        games.append(parse_game_result(f'{folder}/res.json'))

    return games


def main(args):
    folder = args[0]
    repeats = int(args[1]) if len(args) > 1 else 1
    win_threshold_in_percents = int(args[2]) if len(args) > 2 else 100

    games = run_games(folder, repeats)

    win_percent = len(list(filter(lambda x: bool(x.is_winner), games))) * 1.0 / repeats * 100.0
    crashed_games = len(list(filter(lambda x: bool(x.is_crashed), games)))

    for i, g in enumerate(games):
        print(f'Game {i + 1}: {g}')

    if crashed_games != 0:
        sys.exit(f"Strategy crashed in {crashed_games} out of {repeats}")

    if win_percent < win_threshold_in_percents:
        sys.exit(f"Strategy won in {win_percent} which is less than required {win_threshold_in_percents}")

    print('Run is successful :)')


if __name__ == '__main__':
    main(sys.argv[1:])
